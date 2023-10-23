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
import controllers.journeys.income.routes.{HowMuchTradingAllowanceController, TradingAllowanceAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.HowMuchTradingAllowanceFormProvider
import models.{CheckMode, HowMuchTradingAllowance, NormalMode, UserAnswers}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{HowMuchTradingAllowancePage, TurnoverIncomeAmountPage}
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Format.GenericFormat
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.HowMuchTradingAllowanceView

import scala.concurrent.Future

class HowMuchTradingAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider                      = new HowMuchTradingAllowanceFormProvider()
  val maxTradingAllowance: BigDecimal   = 1000.00
  val smallTradingAllowance: BigDecimal = 260.50
  val maxTradingAllowanceString         = maxTradingAllowance.setScale(2).toString()
  val smallTradingAllowanceString       = smallTradingAllowance.setScale(2).toString()
  val formIndividualWithMaxTA           = formProvider("individual", maxTradingAllowanceString)
  val formAgentWithSmallTA              = formProvider("agent", smallTradingAllowanceString)
  val businessId                        = "SJPR05893938418"
  val onwardRoute                       = TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[HowMuchTradingAllowance], allowance: BigDecimal, allowanceString: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formIndividualWithMaxTA, maxTradingAllowance, maxTradingAllowanceString),
    UserScenario(isWelsh = false, isAgent = true, formAgentWithSmallTA, smallTradingAllowance, smallTradingAllowanceString)
  )

  def formTypeToString(form: Form[HowMuchTradingAllowance]): String =
    if (form.equals(formIndividualWithMaxTA)) "max allowance" else "non-max allowance"

  "HowMuchTradingAllowance Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.isAgent)} and using the ${formTypeToString(userScenario.form)}" - {
          "must return OK with the correct view" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val expectedResult =
                view(userScenario.form, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.allowanceString)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TurnoverIncomeAmountPage, userScenario.allowance)
              .success
              .value
              .set(HowMuchTradingAllowancePage, HowMuchTradingAllowance.values.head)
              .success
              .value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(
                userScenario.form.fill(HowMuchTradingAllowance.values.head),
                CheckMode,
                authUserType(userScenario.isAgent),
                taxYear,
                businessId,
                userScenario.allowanceString
              )(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url)

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
            FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", HowMuchTradingAllowance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.isAgent)} and using the ${formTypeToString(userScenario.form)}" - {
          "must return a Bad Request and errors when invalid data is submitted" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.allowanceString)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when an empty form is submitted" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.allowanceString)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
            .withFormUrlEncodedBody(("value", HowMuchTradingAllowance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
