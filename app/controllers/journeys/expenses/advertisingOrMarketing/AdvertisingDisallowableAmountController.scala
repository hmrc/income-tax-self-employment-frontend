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

package controllers.journeys.expenses.advertisingOrMarketing

import controllers.actions._
import controllers.journeys.fillForm
import forms.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AdvertisingDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         service: SelfEmploymentService,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: AdvertisingDisallowableAmountFormProvider,
                                                         view: AdvertisingDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = AdvertisingOrMarketingDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(AdvertisingOrMarketingAmountPage, businessId)
        form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
      } yield Ok(view(form, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleFormError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))

      (for {
        allowableAmount <- request.valueOrFutureRedirectDefault(AdvertisingOrMarketingAmountPage, businessId)
        result = service.defaultHandleForm(
          formProvider(request.userType, allowableAmount),
          page,
          businessId,
          taxYear,
          mode,
          handleFormError(allowableAmount))
      } yield result).merge
  }

}
