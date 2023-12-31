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

package controllers.journeys.expenses.depreciation

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.depreciation.DepreciationDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.Depreciation.Yes
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.tailoring.individualCategories.DepreciationPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import views.html.journeys.expenses.depreciation.DepreciationDisallowableAmountView

import scala.concurrent.Future

class DepreciationDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "DepreciationDisallowableAmountController",
      DepreciationDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.DepreciationDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.DepreciationDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.DepreciationCYAController.onPageLoad(taxYear, businessId)

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(DepreciationPage, Yes, Some(businessId)).success.value

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  def createForm(userType: UserType): Form[BigDecimal] = new DepreciationDisallowableAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[DepreciationDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
