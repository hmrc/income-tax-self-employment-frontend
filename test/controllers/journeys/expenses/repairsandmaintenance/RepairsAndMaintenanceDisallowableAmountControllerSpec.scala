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

package controllers.journeys.expenses.repairsandmaintenance

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountFormProvider
import models.NormalMode
import models.common.{TextAmount, UserType}
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountView

import scala.concurrent.Future

class RepairsAndMaintenanceDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "RepairsAndMaintenanceDisallowableAmountController",
      RepairsAndMaintenanceDisallowableAmountPage
    ) {

  private lazy val allowableAmount: BigDecimal = 500.50

  lazy val onPageLoadRoute: String = routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.RepairsAndMaintenanceDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(RepairsAndMaintenanceAmountPage, allowableAmount, Some(businessId)).success.value

  def createForm(userType: UserType): Form[BigDecimal] = new RepairsAndMaintenanceDisallowableAmountFormProvider()(userType, allowableAmount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[RepairsAndMaintenanceDisallowableAmountView]
    view(form, scenario.mode, scenario.taxYear, scenario.businessId, scenario.userType, TextAmount(formatMoney(allowableAmount))).toString()
  }

}
