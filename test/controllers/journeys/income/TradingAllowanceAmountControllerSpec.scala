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
import controllers.journeys.income.routes.{IncomeCYAController, TradingAllowanceAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TradingAllowanceAmountFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TradingAllowanceAmountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.TradingAllowanceAmountView

import scala.concurrent.Future

class TradingAllowanceAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new TradingAllowanceAmountFormProvider()
  val maxTradingAllowance     = 1000.00
  val smallTradingAllowance   = 400.00
  val validAnswer: BigDecimal = 100
  val formIndividualWithMaxTA = formProvider(UserType.Individual, maxTradingAllowance)
  val formAgentWithSmallTA    = formProvider(UserType.Agent, maxTradingAllowance)
  val onwardRoute             = IncomeCYAController.onPageLoad(taxYear, businessId)

  case class UserScenario(authUserType: UserType, form: Form[BigDecimal])

  val userScenarios = Seq(
    UserScenario(authUserType = UserType.Individual, formIndividualWithMaxTA),
    UserScenario(authUserType = UserType.Agent, formAgentWithSmallTA)
  )

  def formTypeToString(form: Form[BigDecimal]): String =
    if (form.equals(formIndividualWithMaxTA)) "max allowance" else "non-max allowance"

  "Trading allowance amount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when  ${userScenario.authUserType} and using the ${formTypeToString(userScenario.form)}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TradingAllowanceAmountPage, validAnswer, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.authUserType).build()

            running(application) {
              val request = FakeRequest(GET, TradingAllowanceAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val expectedResult = view(userScenario.form.fill(validAnswer), CheckMode, userScenario.authUserType, taxYear, businessId)(
                request,
                messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when valid data is submitted must redirect to the CYA page when" - {
        "in NormalMode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
        "in CheckMode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.authUserType} and using the ${formTypeToString(userScenario.form)}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

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
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-BigDecimal"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "a negative number is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "-23"))

              val boundForm = userScenario.form.bind(Map("value" -> "-23"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "turnover income amount exceeds £100,000,000,000.00" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType).build()

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "100000000000.01"))

              val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
