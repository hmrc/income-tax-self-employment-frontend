/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.journeys.income

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.routes._
import controllers.standard.routes._
import models.NormalMode
import models.common.ModelUtils.userType
import models.common._
import models.database.UserAnswers
import models.journeys.Journey.Income
import models.journeys.income.IncomeJourneyAnswers
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SendJourneyAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.income._
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.income.IncomeCYAView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IncomeCYAController @Inject() (override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     service: SendJourneyAnswersService,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: IncomeCYAView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val user = userType(request.user.isAgent)

    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        IncomeNotCountedAsTurnoverSummary.row(request.userAnswers, taxYear, user, businessId),
        NonTurnoverIncomeAmountSummary.row(request.userAnswers, taxYear, user, businessId),
        TurnoverIncomeAmountSummary.row(request.userAnswers, taxYear, user, businessId),
        AnyOtherIncomeSummary.row(request.userAnswers, taxYear, user, businessId),
        OtherIncomeAmountSummary.row(request.userAnswers, taxYear, user, businessId),
        TurnoverNotTaxableSummary.row(request.userAnswers, taxYear, user, businessId),
        NotTaxableAmountSummary.row(request.userAnswers, taxYear, user, businessId),
        TradingAllowanceSummary.row(request.userAnswers, taxYear, user, businessId),
        howMuchTradingAllowanceSummaryRow(request.userAnswers, taxYear, user, businessId),
        TradingAllowanceAmountSummary.row(request.userAnswers, taxYear, user, businessId)
      )
    )

    Ok(view(taxYear, businessId, summaryList, user))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val journeyAnswers = (request.userAnswers.data \ businessId.value).as[IncomeJourneyAnswers]
      val context        = SubmissionContext(taxYear, Nino(request.user.nino), businessId, Mtditid(request.user.mtditid), Income)

      // Replace redirected error case with a proper implementation when the unhappy path ticket is picked up,
      // however need to do something now
      (for {
        _ <- EitherT(service.sendJourneyAnswers(context, journeyAnswers))
      } yield Redirect(SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode)))
        .leftMap(_ => Redirect(JourneyRecoveryController.onPageLoad()))
        .merge
  }

  private def howMuchTradingAllowanceSummaryRow(userAnswers: UserAnswers, taxYear: TaxYear, authUserType: String, businessId: BusinessId)(implicit
      messages: Messages): Option[SummaryListRow] =
    HowMuchTradingAllowanceSummary.row(userAnswers, taxYear, authUserType, businessId).map {
      case Right(value)    => value
      case Left(exception) => throw exception
    }

}
