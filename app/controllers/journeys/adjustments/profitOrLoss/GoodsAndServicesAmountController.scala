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

package controllers.journeys.adjustments.profitOrLoss

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.standard.CurrencyFormProvider
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.{CheckMode, Mode}
import pages.adjustments.profitOrLoss.{GoodsAndServicesAmountPage, PreviousUnusedLossesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.adjustments.profitOrLoss.GoodsAndServicesAmountView

import javax.inject.{Inject, Singleton}

@Singleton
class GoodsAndServicesAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: CurrencyFormProvider,
                                                  view: GoodsAndServicesAmountView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = GoodsAndServicesAmountPage
  private val form = (userType: UserType, accountingType: AccountingType) => formProvider(page, userType, optAccountingType = accountingType.some)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val accountingType = returnAccountingType(businessId)
      val filledForm     = fillForm(page, businessId, form(request.userType, accountingType))
      Ok(view(filledForm, taxYear, businessId, request.userType, mode, accountingType))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val accountingType          = returnAccountingType(businessId)
      val toCyaIfAllPagesAnswered = if (PreviousUnusedLossesPage.hasAllFurtherAnswers(businessId, request.userAnswers)) CheckMode else mode

      def handleError(formWithErrors: Form[_]): Result = BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode, accountingType))

      selfEmploymentService.defaultHandleForm(form(request.userType, accountingType), page, businessId, taxYear, toCyaIfAllPagesAnswered, handleError)
  }

}
