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

package controllers.journeys.adjustments.profitOrLoss

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import pages.adjustments.profitOrLoss.UnusedLossAmountPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.adjustments.profitOrLoss.UnusedLossAmountView

import javax.inject.{Inject, Singleton}

@Singleton
class UnusedLossAmountController @Inject() (override val messagesApi: MessagesApi,
                                            val controllerComponents: MessagesControllerComponents,
                                            selfEmploymentService: SelfEmploymentService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: CurrencyFormProvider,
                                            view: UnusedLossAmountView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = UnusedLossAmountPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = Some("unusedLossAmount"))

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = fillForm(page, businessId, form(request.userType))
      Ok(view(filledForm, taxYear, businessId, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode))

      selfEmploymentService.defaultHandleForm(form(request.userType), page, businessId, taxYear, mode, handleError)
  }

}
