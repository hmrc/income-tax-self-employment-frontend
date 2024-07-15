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
import controllers.journeys.fillForm
import forms.expenses.staffCosts.StaffCostsDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, TextAmount}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.staffCosts.StaffCostsDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StaffCostsDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                        service: SelfEmploymentService,
                                                        identify: IdentifierAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: StaffCostsDisallowableAmountFormProvider,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        view: StaffCostsDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  private val page = StaffCostsDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request
        .valueOrRedirectDefault(StaffCostsAmountPage, businessId)
        .map { allowableAmount =>
          val preparedForm = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
          Ok(view(preparedForm, mode, request.userType, taxYear, businessId, TextAmount(allowableAmount)))
        }
        .merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(staffCostsAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, TextAmount(staffCostsAmount)))

      request
        .valueOrFutureRedirectDefault(StaffCostsAmountPage, businessId)
        .map { staffCostsAmount =>
          service.defaultHandleForm(formProvider(request.userType, staffCostsAmount), page, businessId, taxYear, mode, handleError(staffCostsAmount))
        }
        .merge
  }

}
