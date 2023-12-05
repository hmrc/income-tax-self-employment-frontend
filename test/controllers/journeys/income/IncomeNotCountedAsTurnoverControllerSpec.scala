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

package controllers.journeys.income

import base.SpecBase
import controllers.journeys.income.routes.{IncomeCYAController, IncomeNotCountedAsTurnoverController, NonTurnoverIncomeAmountController, TurnoverIncomeAmountController}
import forms.income.IncomeNotCountedAsTurnoverFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{IncomeNotCountedAsTurnoverPage, NonTurnoverIncomeAmountPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.IncomeNotCountedAsTurnoverView

import scala.concurrent.Future

class IncomeNotCountedAsTurnoverControllerSpec extends SpecBase with MockitoSugar {

  val formProvider                = new IncomeNotCountedAsTurnoverFormProvider()
  val nonTurnoverIncomeAmountCall = NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val turnoverIncomeAmountCall    = TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val incomeCyaCall               = IncomeCYAController.onPageLoad(taxYear, businessId)

  val onwardRouteNormalMode = (userAnswer: Boolean) => if (userAnswer) nonTurnoverIncomeAmountCall else turnoverIncomeAmountCall
  val onwardRouteCheckMode  = (userAnswer: Boolean) => if (userAnswer) nonTurnoverIncomeAmountCall else incomeCyaCall

  val mockSessionRepository = mock[SessionRepository]

  case class UserScenario(authUserType: UserType, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(authUserType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(authUserType = UserType.Agent, formProvider(UserType.Agent))
  )

  "IncomeNotCountedAsTurnover Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.authUserType}" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[IncomeNotCountedAsTurnoverView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(IncomeNotCountedAsTurnoverPage, true, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[IncomeNotCountedAsTurnoverView]

              val result = route(application, request).value

              val expectedResult = view(userScenario.form.fill(true), CheckMode, userScenario.authUserType, taxYear, businessId)(
                request,
                messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }
    }

    "onSubmit" - {

      "when a user answer 'Yes' is submitted must redirect to the Non-Turnover Income Amount page when in" - {
        "NormalMode" in {

          val userAnswer = true

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRouteNormalMode(userAnswer))),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual nonTurnoverIncomeAmountCall.url
          }
        }

        "CheckMode" in {

          val userAnswer = true

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRouteCheckMode(userAnswer))),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual nonTurnoverIncomeAmountCall.url
          }
        }
      }

      "when a user answer 'No' is submitted must clear NonTurnoverIncomeAmount data and redirect to the" - {
        "Turnover Income Amount page when in NormalMode" in {

          val userAnswer  = false
          val userAnswers = UserAnswers(userAnswersId).set(NonTurnoverIncomeAmountPage, BigDecimal(400), Some(businessId)).success.value

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRouteNormalMode(userAnswer))),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual turnoverIncomeAmountCall.url
            UserAnswers(userAnswersId).get(NonTurnoverIncomeAmountPage, Some(businessId)) mustBe None
          }
        }

        "CYA page when in CheckMode and journey model is now complete" in {

          val userAnswer  = false
          val userAnswers = UserAnswers(userAnswersId).set(NonTurnoverIncomeAmountPage, BigDecimal(400), Some(businessId)).success.value

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRouteCheckMode(userAnswer))),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
            UserAnswers(userAnswersId).get(NonTurnoverIncomeAmountPage, Some(businessId)) mustBe None
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.authUserType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[IncomeNotCountedAsTurnoverView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, IncomeNotCountedAsTurnoverController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-Boolean"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-Boolean"))

              val view = application.injector.instanceOf[IncomeNotCountedAsTurnoverView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }
    }
  }

}
