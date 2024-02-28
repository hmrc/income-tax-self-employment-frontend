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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.capitalallowances.electricVehicleChargePoints.EvcpOnlyForSelfEmploymentFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpOnlyForSelfEmployment
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.electricVehicleChargePoints.EvcpOnlyForSelfEmploymentPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.electricVehicleChargePoints.EvcpOnlyForSelfEmploymentView

class EvcpOnlyForSelfEmploymentControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec("EvcpOnlyForSelfEmploymentController", EvcpOnlyForSelfEmploymentPage) {

  override def onPageLoadCall: Call = routes.EvcpOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.EvcpOnlyForSelfEmploymentController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = models.common.onwardRoute

  override val validAnswer: EvcpOnlyForSelfEmployment = EvcpOnlyForSelfEmployment.Yes

  override def createForm(user: UserType): Form[EvcpOnlyForSelfEmployment] = new EvcpOnlyForSelfEmploymentFormProvider()(user)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[EvcpOnlyForSelfEmploymentView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, businessId.some).success.value

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns filledUserAnswers.asFuture

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}
