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
import controllers.journeys.income.routes.{HowMuchTradingAllowanceController, IncomeCYAController, TradingAllowanceController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TradingAllowanceFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.journeys.income.HowMuchTradingAllowance.LessThan
import models.journeys.income.TradingAllowance
import models.journeys.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{HowMuchTradingAllowancePage, TradingAllowanceAmountPage, TradingAllowancePage}
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.TradingAllowanceView

import scala.concurrent.Future

class TradingAllowanceControllerSpec extends SpecBase with MockitoSugar {

  val formProvider                = new TradingAllowanceFormProvider()
  val howMuchTradingAllowanceCall = HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  val incomeCyaCall               = IncomeCYAController.onPageLoad(taxYear, businessId)

  val onwardRoute = (userAnswer: TradingAllowance) => if (userAnswer.equals(UseTradingAllowance)) howMuchTradingAllowanceCall else incomeCyaCall

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(isWelsh: Boolean, authUserType: UserType, form: Form[TradingAllowance], accountingType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, authUserType = UserType.Individual, formProvider(authUserType(UserType.Individual)), accrual),
    UserScenario(isWelsh = false, authUserType = UserType.Agent, formProvider(authUserType(UserType.Agent)), cash)
  )

  "TradingAllowance Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.authUserType)} and has ${userScenario.accountingType} type accounting" - {
          "must return OK and the correct view" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.authUserType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))
              val request = FakeRequest(GET, TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[TradingAllowanceView]

              val expectedResult =
                view(userScenario.form, NormalMode, authUserType(userScenario.authUserType), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(TradingAllowancePage, TradingAllowance.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.authUserType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, TradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[TradingAllowanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(
                userScenario.form.fill(TradingAllowance.values.head),
                CheckMode,
                authUserType(userScenario.authUserType),
                taxYear,
                businessId,
                userScenario.accountingType)(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the How Much Trading Allowance page when 'UseTradingAllowance' answer is submitted" - {
        "in NormalMode" in {

          val userAnswer = UseTradingAllowance

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual howMuchTradingAllowanceCall.url
          }
        }
        "in CheckMode" in {

          val userAnswer = UseTradingAllowance

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual howMuchTradingAllowanceCall.url
          }
        }
      }

      "must clear any old existing data and redirect to the Income CYA page when 'DeclareExpenses' answer is submitted" - {
        "in NormalMode" in {

          val userAnswer = DeclareExpenses
          val userAnswers = UserAnswers(userAnswersId)
            .set(HowMuchTradingAllowancePage, LessThan, Some(businessId))
            .success
            .value
            .set(TradingAllowanceAmountPage, BigDecimal(400), Some(businessId))
            .success
            .value

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
            UserAnswers(userAnswersId).get(HowMuchTradingAllowancePage, Some(businessId)) mustBe None
            UserAnswers(userAnswersId).get(TradingAllowanceAmountPage, Some(businessId)) mustBe None
          }
        }
        "in CheckMode" in {

          val userAnswer = DeclareExpenses
          val userAnswers = UserAnswers(userAnswersId)
            .set(HowMuchTradingAllowancePage, LessThan, Some(businessId))
            .success
            .value
            .set(TradingAllowanceAmountPage, BigDecimal(400), Some(businessId))
            .success
            .value

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(userAnswer))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

            val request =
              FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
            UserAnswers(userAnswersId).get(HowMuchTradingAllowancePage, Some(businessId)) mustBe None
            UserAnswers(userAnswersId).get(TradingAllowanceAmountPage, Some(businessId)) mustBe None
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${authUserType(userScenario.authUserType)} and has ${userScenario.accountingType} type accounting" - {
          "must return a Bad Request and errors when" - {
            "an empty form is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()
              implicit val messagesApi = application.injector.instanceOf[MessagesApi]

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", ""))

                val boundForm = userScenario.form.bind(Map("value" -> ""))

                val view = application.injector.instanceOf[TradingAllowanceView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, authUserType(userScenario.authUserType), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(langResult) mustEqual expectedResult
              }
            }

            "invalid data is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.authUserType)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()
              implicit val messagesApi = application.injector.instanceOf[MessagesApi]

              running(application) {
                when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

                val request =
                  FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
                    .withFormUrlEncodedBody(("value", "invalid value"))

                val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

                val view = application.injector.instanceOf[TradingAllowanceView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, authUserType(userScenario.authUserType), taxYear, businessId, userScenario.accountingType)(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(langResult) mustEqual expectedResult
              }
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url)
            .withFormUrlEncodedBody(("value", TradingAllowance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
