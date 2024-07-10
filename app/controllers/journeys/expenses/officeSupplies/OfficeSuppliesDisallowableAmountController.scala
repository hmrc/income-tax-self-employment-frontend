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

package controllers.journeys.expenses.officeSupplies

import controllers.actions._
import controllers.journeys.fillForm
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class OfficeSuppliesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                            service: SelfEmploymentService,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: OfficeSuppliesDisallowableAmountFormProvider,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            view: OfficeSuppliesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  private val page = OfficeSuppliesDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request
        .valueOrRedirectDefault(OfficeSuppliesAmountPage, businessId)
        .map { allowableAmount =>
          val form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
          Ok(view(form, mode, taxYear, businessId, request.userType, formatMoney(allowableAmount)))
        }
        .merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, taxYear, businessId, request.userType, formatMoney(allowableAmount)))

      request
        .valueOrFutureRedirectDefault(OfficeSuppliesAmountPage, businessId)
        .map { allowableAmount =>
          service.defaultHandleForm(formProvider(request.userType, allowableAmount), page, businessId, taxYear, mode, handleError(allowableAmount))
        }
        .merge
  }

}
