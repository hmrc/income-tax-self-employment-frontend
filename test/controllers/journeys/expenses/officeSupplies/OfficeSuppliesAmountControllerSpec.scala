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
import forms.expenses.officeSupplies.OfficeSuppliesAmountFormProvider
import models.NormalMode
import models.common.{AccountingType, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OfficeSupplies
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.officeSupplies.OfficeSuppliesAmountPage
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesAmountView

import scala.concurrent.Future

class OfficeSuppliesAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "OfficeSuppliesAmountController",
      OfficeSuppliesAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.OfficeSuppliesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(OfficeSuppliesPage, OfficeSupplies.YesDisallowable, Some(businessId)).success.value

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

  def createForm(userType: UserType): Form[BigDecimal] = new OfficeSuppliesAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OfficeSuppliesAmountView]
    view(form, scenario.mode, scenario.userType, AccountingType.Accrual, scenario.taxYear, scenario.businessId).toString()
  }

}
