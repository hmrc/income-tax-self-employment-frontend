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

package controllers.journeys.expenses.goodsToSellOrUse

import cats.data.EitherT
import controllers.actions._
import controllers.journeys.routes._
import controllers.standard.routes._
import models.NormalMode
import models.common.ModelUtils.userType
import models.common._
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SendJourneyAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountSummary, GoodsToSellOrUseAmountSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GoodsToSellOrUseCYAController @Inject() (override val messagesApi: MessagesApi,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               service: SendJourneyAnswersService,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: GoodsToSellOrUseCYAView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val user = userType(request.user.isAgent)

    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        GoodsToSellOrUseAmountSummary.row(request.userAnswers, taxYear, businessId, user),
        DisallowableGoodsToSellOrUseAmountSummary.row(request.userAnswers, taxYear, businessId, user)
      )
    )

    Ok(view(taxYear, businessId, user, summaryList))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val journeyAnswers = (request.userAnswers.data \ businessId.value).as[GoodsToSellOrUseJourneyAnswers]
      val context        = SubmissionContext(taxYear, Nino(request.user.nino), businessId, Mtditid(request.user.mtditid), ExpensesGoodsToSellOrUse)

      // What we decide to do with the unhappy path of receiving a downstream http error should be implemented at a later
      // date (awaiting a JIRA ticket), however we need to do something now.
      (for {
        _ <- EitherT(service.sendJourneyAnswers(context, journeyAnswers))
      } yield Redirect(SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesGoodsToSellOrUse.toString, NormalMode)))
        .leftMap(_ => Redirect(JourneyRecoveryController.onPageLoad()))
        .merge
  }

}
