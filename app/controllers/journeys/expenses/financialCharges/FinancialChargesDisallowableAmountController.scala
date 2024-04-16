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

package controllers.journeys.expenses.financialCharges

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import forms.expenses.financialCharges.FinancialChargesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.financialCharges.FinancialChargesDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FinancialChargesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                              identify: IdentifierAction,
                                                              getAnswers: DataRetrievalAction,
                                                              requireAnswers: DataRequiredAction,
                                                              service: SelfEmploymentService,
                                                              formProvider: FinancialChargesDisallowableAmountFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: FinancialChargesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = FinancialChargesDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) {
    implicit request =>
      request
        .valueOrRedirectDefault(FinancialChargesAmountPage, businessId)
        .map { allowableAmount =>
          val form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
          Ok(view(form, mode, taxYear, businessId, request.userType, formatMoney(allowableAmount)))
        }
        .merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers).async { implicit request =>
      def handleError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, taxYear, businessId, request.userType, formatMoney(allowableAmount)))

      request
        .valueOrFutureRedirectDefault(FinancialChargesAmountPage, businessId)
        .map { allowableAmount =>
          service.defaultHandleForm(formProvider(request.userType, allowableAmount), page, businessId, taxYear, mode, handleError(allowableAmount))
        }
        .merge
    }

}
