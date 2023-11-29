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
import controllers.journeys.income.routes.{AnyOtherIncomeController, IncomeCYAController, TurnoverIncomeAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TurnoverIncomeAmountFormProvider
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TurnoverIncomeAmountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.TurnoverIncomeAmountView

import scala.concurrent.Future

class TurnoverIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new TurnoverIncomeAmountFormProvider()
  val validAnswer: BigDecimal = 100
  val onwardRoute             = AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode)
  val cyaCall                 = IncomeCYAController.onPageLoad(taxYear, businessId)

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[BigDecimal], accountingType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider(individual), accrual),
    UserScenario(isWelsh = false, isAgent = true, formProvider(agent), cash)
  )

  "TurnoverIncomeAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${userType(userScenario.isAgent)} and has ${userScenario.accountingType} type accounting" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[TurnoverIncomeAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, validAnswer, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[TurnoverIncomeAmountView]

              val result = route(application, request).value

              val expectedResult = view(
                userScenario.form.fill(validAnswer),
                CheckMode,
                userType(userScenario.isAgent),
                taxYear,
                businessId,
                userScenario.accountingType)(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when valid data is submitted must redirect to the" - {
        "Any Other Income page when in NormalMode" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute)),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "CYA page when in CheckMode and income model is now completed" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(cyaCall)),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual cyaCall.url
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${userType(userScenario.isAgent)} and has ${userScenario.accountingType} type accounting" - {
          "must return a Bad Request and errors when" - {
            "an empty form is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", ""))

                val boundForm = userScenario.form.bind(Map("value" -> ""))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value
                val expectedResult =
                  view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(result) mustEqual expectedResult
              }
            }

            "invalid data is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", "non-BigDecimal"))

                val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value
                val expectedResult =
                  view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(result) mustEqual expectedResult
              }
            }

            "a negative number is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", "-23"))

                val boundForm = userScenario.form.bind(Map("value" -> "-23"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value
                val expectedResult =
                  view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(result) mustEqual expectedResult
              }
            }

            "turnover income amount exceeds £100,000,000,000.00" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", "100000000000.01"))

                val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value
                val expectedResult =
                  view(boundForm, NormalMode, userType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(result) mustEqual expectedResult
              }
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, TurnoverIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
