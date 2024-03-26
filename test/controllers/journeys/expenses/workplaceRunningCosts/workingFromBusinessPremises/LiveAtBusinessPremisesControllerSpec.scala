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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.standard.BooleanFormProvider
import models.common.UserType
import models.common.UserType.Individual
import models.database.UserAnswers
import models.{CheckMode, Mode, NormalMode}
import navigation.{FakeWorkplaceRunningCostsNavigatorTwoRoutesNavigator, WorkplaceRunningCostsNavigator}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableFor4
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesView

import scala.concurrent.Future

class LiveAtBusinessPremisesControllerSpec extends BooleanGetAndPostQuestionBaseSpec("LiveAtBusinessPremisesController", LiveAtBusinessPremisesPage) {

  override def onPageLoadCall: Call = routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = submissionCall(NormalMode)
  override def onwardRoute: Call    = expectedRedirectCall(NormalMode)

  private def submissionCall(mode: Mode): Call       = routes.LiveAtBusinessPremisesController.onSubmit(taxYear, businessId, mode)
  private def expectedRedirectCall(mode: Mode): Call = routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, mode)

  override def pageAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[WorkplaceRunningCostsNavigator].toInstance(
      new FakeWorkplaceRunningCostsNavigatorTwoRoutesNavigator(onwardRoute, expectedRedirectCall(CheckMode)))
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(pageAnswers)

  override def createForm(userType: UserType) = new BooleanFormProvider()(LiveAtBusinessPremisesPage, userType)

  override def expectedView(expectedForm: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[LiveAtBusinessPremisesView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  private lazy val cases: TableFor4[Option[Boolean], Boolean, Mode, Mode] = Table(
    ("prevAnswer", "currAnswer", "submissionMode", "expectedRedirectMode"),
    (None, true, NormalMode, NormalMode),
    (None, false, NormalMode, NormalMode),
    (Some(true), true, NormalMode, NormalMode),
    (Some(true), false, NormalMode, NormalMode),
    (Some(false), true, NormalMode, NormalMode),
    (Some(false), false, NormalMode, NormalMode),
    (Some(true), true, CheckMode, CheckMode),
    (Some(true), false, CheckMode, CheckMode),
    (Some(false), true, CheckMode, NormalMode),
    (Some(false), false, CheckMode, CheckMode)
  )

  "On successful submission, redirect should have the correct Mode type" - {
    forAll(cases) { case (prevAnswer, currAnswer, submissionMode, expectedRedirectMode) =>
      val answers: UserAnswers = prevAnswer.fold(emptyUserAnswers)(ans => emptyUserAnswers.set(page, ans, Some(businessId)).success.value)

      s"when previous answer was $prevAnswer, submitted answer is $currAnswer and submission mode was $submissionMode" in
        new TestScenario(Individual, answers = answers.some) {
          running(application) {
            val request = FakeRequest(POST, submissionCall(submissionMode).url).withFormUrlEncodedBody(("value", currAnswer.toString))
            val result  = route(application, request).value

            redirectLocation(result).value shouldBe expectedRedirectCall(expectedRedirectMode).url
          }
        }
    }
  }
}
