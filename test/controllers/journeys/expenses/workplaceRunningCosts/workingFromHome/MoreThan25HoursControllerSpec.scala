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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursFormProvider
import models.common.UserType
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours.{No, Yes}
import models.{CheckMode, Mode, NormalMode}
import navigation.{ExpensesNavigator, FakeExpensesTwoRoutesNavigator}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableFor4
import pages.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.{JsString, Writes}
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursView

import scala.concurrent.Future

class MoreThan25HoursControllerSpec extends RadioButtonGetAndPostQuestionBaseSpec[MoreThan25Hours]("MoreThan25HoursController", MoreThan25HoursPage) {

  override lazy val onPageLoadCall: Call         = routes.MoreThan25HoursController.onPageLoad(taxYear, businessId, NormalMode)
  override lazy val onSubmitCall: Call           = submissionCall(NormalMode)
  override lazy val onwardRoute: Call            = expectedRedirectCall(NormalMode)
  override lazy val validAnswer: MoreThan25Hours = Yes

  private def submissionCall(mode: Mode): Call       = routes.MoreThan25HoursController.onSubmit(taxYear, businessId, mode)
  private def expectedRedirectCall(mode: Mode): Call = routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, mode)

  override val filledUserAnswers: UserAnswers = blankUserAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesTwoRoutesNavigator(onwardRoute, expectedRedirectCall(CheckMode)))
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def createForm(userType: UserType): Form[MoreThan25Hours] = new MoreThan25HoursFormProvider()(userType)

  override def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[MoreThan25HoursView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  private lazy val cases: TableFor4[Option[MoreThan25Hours], MoreThan25Hours, Mode, Mode] = Table(
    ("prevAnswer", "currAnswer", "submissionMode", "expectedRedirectMode"),
    (None, Yes, NormalMode, NormalMode),
    (None, No, NormalMode, NormalMode),
    (Some(Yes), Yes, NormalMode, NormalMode),
    (Some(Yes), No, NormalMode, NormalMode),
    (Some(No), Yes, NormalMode, NormalMode),
    (Some(No), No, NormalMode, NormalMode),
    (Some(Yes), Yes, CheckMode, CheckMode),
    (Some(Yes), No, CheckMode, CheckMode),
    (Some(No), Yes, CheckMode, NormalMode),
    (Some(No), No, CheckMode, CheckMode)
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
