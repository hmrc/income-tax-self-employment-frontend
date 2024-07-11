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

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.journeys.fillForm
import controllers.standard.routes
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import pages.expenses.staffCosts.StaffCostsAmountPage
import pages.expenses.tailoring.individualCategories.DisallowableStaffCostsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.staffCosts.StaffCostsAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StaffCostsAmountController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: CurrencyFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: StaffCostsAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = StaffCostsAmountPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = page.toString.some)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(DisallowableStaffCostsPage, Some(businessId)) match {
        case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Some(disallowableStaffCosts) =>
          val filledForm = fillForm(page, businessId, form(request.userType))
          Ok(view(filledForm, mode, request.userType, taxYear, businessId, disallowableStaffCosts))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(disallowableStaffCosts: Boolean)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, disallowableStaffCosts))

      request
        .valueOrFutureRedirectDefault(DisallowableStaffCostsPage, businessId)
        .map { disallowableStaffCosts =>
          selfEmploymentService.defaultHandleForm(form(request.userType), page, businessId, taxYear, mode, handleError(disallowableStaffCosts))
        }
        .merge
  }

}
