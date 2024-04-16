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

package controllers.journeys.expenses.irrecoverableDebts

import controllers.actions._
import controllers.journeys.fillForm
import forms.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IrrecoverableDebtsDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                                selfEmploymentService: SelfEmploymentService,
                                                                identify: IdentifierAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                formProvider: IrrecoverableDebtsDisallowableAmountFormProvider,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                view: IrrecoverableDebtsDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  private val page = IrrecoverableDebtsDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request
        .valueOrRedirectDefault(IrrecoverableDebtsAmountPage, businessId)
        .map { allowableAmount =>
          val form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
          Ok(view(form, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))
        }
        .merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))

      request
        .valueOrFutureRedirectDefault(IrrecoverableDebtsAmountPage, businessId)
        .map { allowableAmount =>
          selfEmploymentService
            .defaultHandleForm(formProvider(request.userType, allowableAmount), page, businessId, taxYear, mode, handleError(allowableAmount))
        }
        .merge
  }
}
