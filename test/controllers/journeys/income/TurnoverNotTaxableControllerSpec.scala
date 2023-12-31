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
import controllers.journeys.income.routes.{IncomeCYAController, NotTaxableAmountController, TradingAllowanceController, TurnoverNotTaxableController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TurnoverNotTaxableFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{NotTaxableAmountPage, TurnoverNotTaxablePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.TurnoverNotTaxableView

import scala.concurrent.Future

class TurnoverNotTaxableControllerSpec extends SpecBase with MockitoSugar {

  val formProvider         = new TurnoverNotTaxableFormProvider()
  val notTaxableAmountCall = NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val tradingAllowanceCall = TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  val cyaCall              = IncomeCYAController.onPageLoad(taxYear, businessId)

  val onwardRouteNormalMode = (userAnswer: Boolean) => if (userAnswer) notTaxableAmountCall else tradingAllowanceCall
  val onwardRouteCheckMode  = (userAnswer: Boolean) => if (userAnswer) notTaxableAmountCall else cyaCall

  case class UserScenario(userType: UserType, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(userType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(userType = UserType.Agent, formProvider(UserType.Agent))
  )

  val mockSessionRepository = mock[SessionRepository]

  "TurnoverNotTaxable Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[TurnoverNotTaxableView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverNotTaxablePage, true, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, TurnoverNotTaxableController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[TurnoverNotTaxableView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form.fill(true), CheckMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when a user answer 'Yes' is submitted must redirect to the Not Taxable Amount page when in" - {
        val userAnswer = true

        "NormalMode" in {

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
              FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual notTaxableAmountCall.url
          }
        }

        "CheckMode" in {

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
              FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual notTaxableAmountCall.url
          }
        }
      }

      "when a user answer 'No' is submitted must clear NotTaxableAmount data and redirect to the" - {
        val userAnswer = false

        "Trading Allowance page when in NormalMode" in {

          val userAnswers = UserAnswers(userAnswersId).set(NotTaxableAmountPage, BigDecimal(400), Some(businessId)).success.value

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
              FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceCall.url
            UserAnswers(userAnswersId).get(NotTaxableAmountPage, Some(businessId)) mustBe None
          }
        }

        "CYA page when in CheckMode and journey model is now complete" in {

          val userAnswers = UserAnswers(userAnswersId).set(NotTaxableAmountPage, BigDecimal(400), Some(businessId)).success.value

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
              FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual cyaCall.url
            UserAnswers(userAnswersId).get(NotTaxableAmountPage, Some(businessId)) mustBe None
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[TurnoverNotTaxableView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-Boolean"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-Boolean"))

              val view = application.injector.instanceOf[TurnoverNotTaxableView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, TurnoverNotTaxableController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
