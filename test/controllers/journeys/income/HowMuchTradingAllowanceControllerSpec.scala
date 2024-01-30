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
import controllers.journeys.income.routes.{HowMuchTradingAllowanceController, IncomeCYAController, TradingAllowanceAmountController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.HowMuchTradingAllowanceFormProvider
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.income.HowMuchTradingAllowance
import models.journeys.income.HowMuchTradingAllowance.{LessThan, Maximum}
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.matchers.MacroBasedMatchers
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{HowMuchTradingAllowancePage, TradingAllowanceAmountPage, TurnoverIncomeAmountPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import views.html.journeys.income.HowMuchTradingAllowanceView

import scala.concurrent.Future

class HowMuchTradingAllowanceControllerSpec extends SpecBase with MockitoSugar with MacroBasedMatchers {

  val formProvider                      = new HowMuchTradingAllowanceFormProvider()
  val maxTradingAllowance: BigDecimal   = 1000.00
  val smallTradingAllowance: BigDecimal = 260.50
  val maxTradingAllowanceString         = "1,000"
  val smallTradingAllowanceString       = "260.50"
  val formIndividualWithMaxTA           = formProvider(UserType.Individual, maxTradingAllowanceString)
  val formAgentWithSmallTA              = formProvider(UserType.Agent, smallTradingAllowanceString)
  val tradingAllowanceAmountCall        = TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val incomeCyaCall                     = IncomeCYAController.onPageLoad(taxYear, businessId)

  val onwardRoute = (userAnswer: HowMuchTradingAllowance) => if (userAnswer.equals(LessThan)) tradingAllowanceAmountCall else incomeCyaCall

  val mockService = mock[SelfEmploymentService]

  case class UserScenario(userType: UserType, form: Form[HowMuchTradingAllowance], allowance: BigDecimal, allowanceString: String)

  val userScenarios = Seq(
    UserScenario(userType = UserType.Individual, formIndividualWithMaxTA, maxTradingAllowance, maxTradingAllowanceString),
    UserScenario(userType = UserType.Agent, formAgentWithSmallTA, smallTradingAllowance, smallTradingAllowanceString)
  )

  val userAnswers = UserAnswers(userAnswersId, Json.obj(businessId.value -> Json.obj("turnoverIncomeAmount" -> 500.00)))

  def formTypeToString(form: Form[HowMuchTradingAllowance]): String =
    if (form.equals(formIndividualWithMaxTA)) "max allowance" else "non-max allowance"

  // TODO Clean these tests up, overly convoluted.
  "HowMuchTradingAllowance Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using the ${formTypeToString(userScenario.form)}" - {
          "must return OK with the correct view" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId, userScenario.allowanceString)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TurnoverIncomeAmountPage, userScenario.allowance, Some(businessId))
              .success
              .value
              .set(HowMuchTradingAllowancePage, HowMuchTradingAllowance.values.head, Some(businessId))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val expectedResult = view(
                userScenario.form.fill(HowMuchTradingAllowance.values.head),
                CheckMode,
                userScenario.userType,
                taxYear,
                businessId,
                userScenario.allowanceString
              )(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

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

      "must redirect to the Trading Allowance Amount page when 'LessThan' answer is submitted" - {
        "in NormalMode" in {

          val userAnswer = LessThan

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(userAnswers)

          running(application) {
            val request =
              FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceAmountCall.url
          }
        }
        "in CheckMode" in {

          val userAnswer = LessThan

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(userAnswers)

          running(application) {
            val request =
              FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceAmountCall.url
          }
        }
      }

      "must redirect to the Income CYA page when 'Maximum' answer is submitted" - {
        "in NormalMode" in {

          val userAnswer = Maximum

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(userAnswers)

          running(application) {

            val request =
              FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
          }
        }
        "in CheckMode" in {

          val userAnswer = Maximum

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(userAnswers)

          running(application) {

            val request =
              FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
            UserAnswers(userAnswersId).get(TradingAllowanceAmountPage, Some(businessId)) mustBe None
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using the ${formTypeToString(userScenario.form)}" - {
          "must return a Bad Request and errors when invalid data is submitted" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.allowanceString)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when an empty form is submitted" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, userScenario.allowance, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, HowMuchTradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.allowanceString)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

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
