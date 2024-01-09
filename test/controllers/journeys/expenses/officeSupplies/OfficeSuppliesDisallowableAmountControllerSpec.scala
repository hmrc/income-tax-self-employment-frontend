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

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n._
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesDisallowableAmountView

import scala.concurrent.Future

class OfficeSuppliesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "OfficeSuppliesDisallowableAmountController",
      OfficeSuppliesDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.OfficeSuppliesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(OfficeSuppliesAmountPage, allowableAmount, Some(businessId)).success.value

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  private lazy val allowableAmount: BigDecimal = 1000

  def createForm(userType: UserType): Form[BigDecimal] = new OfficeSuppliesDisallowableAmountFormProvider()(userType, allowableAmount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OfficeSuppliesDisallowableAmountView]
    view(form, scenario.mode, scenario.taxYear, scenario.businessId, scenario.userType, formatMoney(allowableAmount)).toString()
  }

}
