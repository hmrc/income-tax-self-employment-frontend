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

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import controllers.{handleResultT, returnAccountingType}
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.adjustments.ProfitOrLoss
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.capitalallowances.AssetBasedAllowanceSummary.buildNetProfitOrLossTable
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

  private val formattedNetProfitOrLossAmount = (netAmount: BigDecimal) => formatSumMoneyNoNegative(List(netAmount))

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val result = service.getNetBusinessProfitOrLossValues(taxYear, request.nino, businessId, request.mtditid).map { profitOrLossValues =>
        val profitOrLoss         = profitOrLossValues.netProfitOrLoss
        val netProfitOrLossTable = buildNetProfitOrLossTable(profitOrLossValues)
        val filledForm           = fillForm(page, businessId, formProvider(page, request.userType))
        val formattedNetAmount   = formattedNetProfitOrLossAmount(profitOrLossValues.netProfitOrLossAmount)

        Ok(
          view(
            filledForm,
            mode,
            request.userType,
            taxYear,
            returnAccountingType(businessId),
            profitOrLoss,
            businessId,
            formattedNetAmount,
            netProfitOrLossTable))
      }
      handleResultT(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(profitOrLoss: ProfitOrLoss, netProfitOrLossTable: Table, formattedNetAmount: String): Future[Result] =
        formProvider(page, request.userType)
          .bindFromRequest()
          .fold(
            formErrors => handleFormError(formErrors, profitOrLoss, netProfitOrLossTable, formattedNetAmount),
            value => handleFormSuccess(value)
          )

      def handleFormError(formWithErrors: Form[Boolean],
                          profitOrLoss: ProfitOrLoss,
                          netProfitOrLossTable: Table,
                          formattedNetAmount: String): Future[Result] = Future.successful(
        BadRequest(
          view(
            formWithErrors,
            mode,
            request.userType,
            taxYear,
            returnAccountingType(businessId),
            profitOrLoss,
            businessId,
            formattedNetAmount,
            netProfitOrLossTable)))
      def handleFormSuccess(answer: Boolean): Future[Result] =
        for {
          (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, request, mode, businessId)
          updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, ClaimCapitalAllowancesPage)
        } yield Redirect(navigator.nextPage(ClaimCapitalAllowancesPage, redirectMode, updatedUserAnswers, taxYear, businessId))

      val resultT: EitherT[Future, ServiceError, Result] = for {
        profitOrLossValues <- service.getNetBusinessProfitOrLossValues(taxYear, request.nino, businessId, request.mtditid)
        netProfitOrLossTable = buildNetProfitOrLossTable(profitOrLossValues)
        formattedNetAmount   = formattedNetProfitOrLossAmount(profitOrLossValues.netProfitOrLossAmount)
        result               <- EitherT.right[ServiceError](handleForm(profitOrLossValues.netProfitOrLoss, netProfitOrLossTable, formattedNetAmount))
      } yield result
      handleResultT(resultT)
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
