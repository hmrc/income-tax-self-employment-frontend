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

package controllers.journeys.capitalallowances.zeroEmissionCars

import cats.implicits.catsSyntaxEitherId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.redirectJourneyRecovery
import forms.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaimFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE.{DifferentAmount, Fifty, Ten, TwentyFive}
import models.journeys.capitalallowances.zeroEmissionCars.ZecUsedForSelfEmployment.{No, Yes}
import models.journeys.capitalallowances.zeroEmissionCars.{ZecHowMuchDoYouWantToClaim, ZecUseOutsideSE, ZecUsedForSelfEmployment}
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.WorkplaceRunningCostsNavigator
import pages.capitalallowances.zeroEmissionCars.{ZecHowMuchDoYouWantToClaimPage, ZecUseOutsideSEPage, ZecUsedForSelfEmploymentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.workplaceRunningCosts.WfbpFlatRateViewModel.calculateFlatRate
import views.html.journeys.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaimView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZecHowMuchDoYouWantToClaimController @Inject()(override val messagesApi: MessagesApi,
                                                     service: SelfEmploymentService,
                                                     navigator: WorkplaceRunningCostsNavigator,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: ZecHowMuchDoYouWantToClaimFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: ZecHowMuchDoYouWantToClaimView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      calculateFullCost(request, businessId) match {
        case Left(redirect) => redirect
        case Right(fullCost) =>
          val form       = formProvider(request.userType, fullCost)
          val filledForm = request.getValue(ZecHowMuchDoYouWantToClaimPage, businessId).fold(form)(form.fill)

          Ok(view(filledForm, mode, request.userType, taxYear, businessId, fullCost))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: ZecHowMuchDoYouWantToClaim, flatRate: BigDecimal): Future[Result] =
        for {
          (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, flatRate, request, mode, businessId)
          updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, ZecHowMuchDoYouWantToClaimPage)
        } yield Redirect(navigator.nextPage(ZecHowMuchDoYouWantToClaimPage, redirectMode, updatedUserAnswers, taxYear, businessId))

      calculateFullCost(request, businessId) match {
        case Left(redirect) => Future(redirect)
        case Right(fullCost) =>
          formProvider(request.userType, fullCost)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, fullCost))),
              value => handleSuccess(value, fullCost)
            )
      }
  }

  private def calculateFullCost(request: DataRequest[AnyContent], businessId: BusinessId): Either[Result, BigDecimal] = {
    def calculate(totalCost: BigDecimal, percentage: ZecUseOutsideSE): Either[Result, BigDecimal] =
      percentage match {
        case Ten => (0.1 * totalCost).asRight
        case TwentyFive => (0.25 * totalCost).asRight
        case Fifty => (0.5 * totalCost).asRight
        case DifferentAmount =>
          request.valueOrRedirectDefault(ZecUseOutsideSEDifferentAmountPage, businessId) map (_ * totalCost)
      }
    val totalCost: Option[BigDecimal] = request.getValue(ZecTotalCostOfCarPage, businessId)
    val onlyForSelfEmployment: Option[ZecUsedForSelfEmployment] = request.getValue(ZecUsedForSelfEmploymentPage, businessId)
    val percentage: Option[ZecUseOutsideSE] = request.getValue(ZecUseOutsideSEPage, businessId)
    (totalCost, onlyForSelfEmployment, percentage) match {
      case (Some(totalCost), Some(Yes), _) => totalCost.asRight
      case (Some(totalCost), Some(No), Some(percentage)) => calculate(totalCost, percentage)
      case _ => redirectJourneyRecovery().asLeft
    }
  }

}
