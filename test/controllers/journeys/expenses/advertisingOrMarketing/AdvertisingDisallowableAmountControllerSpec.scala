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

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.tailoring
import forms.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountView

import scala.concurrent.Future

class AdvertisingDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "AdvertisingDisallowableAmountController",
      AdvertisingOrMarketingDisallowableAmountPage
    ) {

  private lazy val allowableAmount: BigDecimal = 500.50

  lazy val onPageLoadRoute: String = routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.AdvertisingDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(AdvertisingOrMarketingAmountPage, allowableAmount, Some(businessId)).success.value

  def createForm(userType: UserType): Form[BigDecimal] = new AdvertisingDisallowableAmountFormProvider()(userType, allowableAmount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[AdvertisingDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, formatMoney(allowableAmount)).toString()
  }

}
