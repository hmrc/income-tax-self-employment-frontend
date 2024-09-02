/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.capitalallowances.tailoring

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.capitalallowances.AssetBasedAllowanceSummary.buildCarsAndAssetBasedAllowanceTable
import views.html.journeys.capitalallowances.tailoring.ClaimCapitalAllowancesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClaimCapitalAllowancesController @Inject() (override val messagesApi: MessagesApi,
                                                  navigator: CapitalAllowancesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  service: SelfEmploymentService,
                                                  formProvider: BooleanFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: ClaimCapitalAllowancesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = ClaimCapitalAllowancesPage

  private val netAmount          = BigDecimal(12345.67)
  private val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val yourBuildCarsAndAssetBasedAllowanceTable = buildCarsAndAssetBasedAllowanceTable()

      val form = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(
        view(
          form,
          mode,
          request.userType,
          taxYear,
          returnAccountingType(businessId),
          businessId,
          formattedNetAmount,
          yourBuildCarsAndAssetBasedAllowanceTable))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val yourBuildCarsAndAssetBasedAllowanceTable = buildCarsAndAssetBasedAllowanceTable()
      def handleSuccess(answer: Boolean): Future[Result] =
        for {
          (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, request, mode, businessId)
          updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, ClaimCapitalAllowancesPage)
        } yield Redirect(navigator.nextPage(ClaimCapitalAllowancesPage, redirectMode, updatedUserAnswers, taxYear, businessId))

      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formErrors =>
            Future.successful(
              BadRequest(
                view(
                  formErrors,
                  mode,
                  request.userType,
                  taxYear,
                  returnAccountingType(businessId),
                  businessId,
                  formattedNetAmount,
                  yourBuildCarsAndAssetBasedAllowanceTable))),
          value => handleSuccess(value)
        )
  }

  private def handleGatewayQuestion(currentAnswer: Boolean,
                                    request: DataRequest[_],
                                    mode: Mode,
                                    businessId: BusinessId): Future[(UserAnswers, Mode)] = {
    val clearUserAnswerDataIfNeeded =
      if (currentAnswer) Future(request.userAnswers)
      else {
        Future.fromTry(request.userAnswers.set(SelectCapitalAllowancesPage, Set.empty[CapitalAllowances], Some(businessId)))
      }
    val redirectMode = request.getValue(ClaimCapitalAllowancesPage, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case _                            => mode
    }
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

}
