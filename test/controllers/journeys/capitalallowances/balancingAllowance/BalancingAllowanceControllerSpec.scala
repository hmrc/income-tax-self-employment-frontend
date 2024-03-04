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

package controllers.journeys.capitalallowances.balancingAllowance

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.capitalallowances.balancingAllowance.BalancingAllowanceFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.balancingAllowance.BalancingAllowancePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.balancingAllowance.BalancingAllowanceView

class BalancingAllowanceControllerSpec extends RadioButtonGetAndPostQuestionBaseSpec("BalancingAllowanceController", BalancingAllowancePage) {

  override def onPageLoadCall: Call = routes.BalancingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.BalancingAllowanceController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = models.common.onwardRoute

  override val validAnswer = true

  override def createForm(user: UserType): Form[Boolean] = new BalancingAllowanceFormProvider()(user)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[BalancingAllowanceView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, businessId.some).success.value

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns filledUserAnswers.asFuture

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}
