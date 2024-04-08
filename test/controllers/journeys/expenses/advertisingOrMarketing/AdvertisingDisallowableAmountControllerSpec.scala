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

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.tailoring
import forms.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.requests.DataRequest
import org.mockito.IdiomaticMockito.StubbingOps
import pages.OneQuestionPage
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountView

class AdvertisingDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "AdvertisingDisallowableAmountController",
      AdvertisingOrMarketingDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.AdvertisingDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)

  override def baseAnswers: UserAnswers = emptyUserAnswers.set(AdvertisingOrMarketingAmountPage, amount, Some(businessId)).success.value

  override def createForm(userType: UserType): Form[BigDecimal] = new AdvertisingDisallowableAmountFormProvider()(userType, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[AdvertisingDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, formatMoney(amount)).toString()
  }

  mockService.persistAnswerAndRedirect(
    *[OneQuestionPage[BigDecimal]],
    *[BusinessId],
    *[DataRequest[_]],
    *,
    *[TaxYear],
    *[Mode]
  ) returns Redirect(onwardRoute).asFuture

}
