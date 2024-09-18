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

import controllers.actions._
import controllers.journeys.fillForm
import forms.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossFormProvider
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss
import pages.adjustments.profitOrLoss.{CarryLossForwardPage, WhatDoYouWantToDoWithLossPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.adjustments.profitOrLoss.{CarryLossForwardView, WhatDoYouWantToDoWithLossView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CurrentYearLossController @Inject() (override val messagesApi: MessagesApi,
                                           val controllerComponents: MessagesControllerComponents,
                                           service: SelfEmploymentService,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: WhatDoYouWantToDoWithLossFormProvider,
                                           booleanFormProvider: BooleanFormProvider,
                                           whatDoYouWantToDoWithLossView: WhatDoYouWantToDoWithLossView,
                                           carryLossForwardView: CarryLossForwardView)
    extends FrontendBaseController
    with I18nSupport {

  private val whatDoYouWantToDoWithLossPage = WhatDoYouWantToDoWithLossPage
  private val carryLossForwardPage          = CarryLossForwardPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      // TODO retrieve whether the user has other incomes from API 2085 and pension income in SASS-9566
      val hasOtherIncomes = false
      if (hasOtherIncomes) {
        val filledForm = fillForm(whatDoYouWantToDoWithLossPage, businessId, formProvider(whatDoYouWantToDoWithLossPage, request.userType))
        Ok(whatDoYouWantToDoWithLossView(filledForm, taxYear, businessId, request.userType, mode))
      } else {
        val filledForm = fillForm(carryLossForwardPage, businessId, booleanFormProvider(carryLossForwardPage, request.userType))
        Ok(carryLossForwardView(filledForm, taxYear, businessId, request.userType, mode))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      // TODO retrieve whether the user has other incomes from API 2085 and pension income in SASS-9566
      val hasOtherIncomes = false
      if (hasOtherIncomes) {
        def handleError(formWithErrors: Form[_]): Result = BadRequest(
          whatDoYouWantToDoWithLossView(formWithErrors, taxYear, businessId, request.userType, mode)
        )
        def handleSuccess(answer: Set[WhatDoYouWantToDoWithLoss]): Future[Result] =
          service.persistAnswerAndRedirect(whatDoYouWantToDoWithLossPage, businessId, request, answer, taxYear, mode)

        service.handleForm(formProvider(whatDoYouWantToDoWithLossPage, request.userType), handleError, handleSuccess)
      } else {
        def handleError(formWithErrors: Form[_]): Result =
          BadRequest(carryLossForwardView(formWithErrors, taxYear, businessId, request.userType, mode))

        service.defaultHandleForm(
          booleanFormProvider(carryLossForwardPage, request.userType),
          carryLossForwardPage,
          businessId,
          taxYear,
          mode,
          handleError)
      }
  }
}
