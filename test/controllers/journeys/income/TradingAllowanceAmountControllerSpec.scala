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
import controllers.journeys.income.routes.{CheckYourIncomeController, TradingAllowanceAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TradingAllowanceAmountFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TradingAllowanceAmountPage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
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
  val formIndividualWithMaxTA = formProvider("individual", maxTradingAllowance)
  val formAgentWithSmallTA    = formProvider("agent", maxTradingAllowance)
  val businessId              = "SJPR05893938418"
  val onwardRoute             = CheckYourIncomeController.onPageLoad(taxYear, businessId)

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[BigDecimal])

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formIndividualWithMaxTA),
    UserScenario(isWelsh = false, isAgent = true, formAgentWithSmallTA)
  )

  def formTypeToString(form: Form[BigDecimal]): String =
    if (form.equals(formIndividualWithMaxTA)) "max allowance" else "non-max allowance"

  "Trading allowance amount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.isAgent)} and using the ${formTypeToString(userScenario.form)}" - {
          "must return OK and the correct view for a GET" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TradingAllowanceAmountPage, validAnswer, Some(businessId)).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, TradingAllowanceAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(userScenario.form.fill(validAnswer), CheckMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
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

      "must redirect to the next page when valid data is submitted" in {

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

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.isAgent)} and using the ${formTypeToString(userScenario.form)}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-BigDecimal"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "a negative number is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "-23"))

              val boundForm = userScenario.form.bind(Map("value" -> "-23"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "turnover income amount exceeds £100,000,000,000.00" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "100000000000.01"))

              val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

              val view = application.injector.instanceOf[TradingAllowanceAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
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
