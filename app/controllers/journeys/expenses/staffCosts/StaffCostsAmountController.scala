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

package controllers.journeys.expenses.staffCosts

import controllers.actions._
import controllers.standard.routes
import forms.expenses.staffCosts.StaffCostsAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import models.journeys.expenses.DisallowableStaffCosts
import navigation.ExpensesNavigator
import pages.expenses.staffCosts.StaffCostsAmountPage
import pages.expenses.tailoring.DisallowableStaffCostsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.staffCosts.StaffCostsAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StaffCostsAmountController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentServiceBase,
                                            navigator: ExpensesNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: StaffCostsAmountFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: StaffCostsAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(DisallowableStaffCostsPage, Some(businessId)) match {
        case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Some(disallowableStaffCosts) =>
          val preparedForm = request.userAnswers.get(StaffCostsAmountPage, Some(businessId)) match {
            case None        => formProvider(request.userType)
            case Some(value) => formProvider(request.userType).fill(value)
          }

          Ok(view(preparedForm, mode, request.userType, taxYear, businessId, disallowableStaffCosts))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(accountingType: String, disallowableStaffCosts: DisallowableStaffCosts): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formWithErrors => handleError(formWithErrors, disallowableStaffCosts),
            value => handleSuccess(value, AccountingType.withName(accountingType.toUpperCase))
          )
      def handleError(formWithErrors: Form[_], disallowableStaffCosts: DisallowableStaffCosts): Future[Result] =
        Future.successful(
          BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, disallowableStaffCosts))
        )
      def handleSuccess(value: BigDecimal, accountingType: AccountingType): Future[Result] =
        selfEmploymentService
          .saveAnswer(businessId, request.userAnswers, value, StaffCostsAmountPage)
          .map(updated => Redirect(navigator.nextPage(StaffCostsAmountPage, mode, updated, taxYear, businessId, Some(accountingType))))

      val getDisallowableStaffCosts = request.userAnswers.get(DisallowableStaffCostsPage, Some(businessId))

      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap { getAccountingType =>
        (getAccountingType, getDisallowableStaffCosts) match {
          case (Right(accountingType), Some(disallowableStaffCosts)) =>
            handleForm(accountingType, disallowableStaffCosts)
          case (_, _) =>
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }

      }
  }

}
