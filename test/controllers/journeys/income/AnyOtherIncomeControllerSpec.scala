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
import controllers.journeys.income.routes.{
  AnyOtherIncomeController,
  OtherIncomeAmountController,
  TradingAllowanceController,
  TurnoverNotTaxableController
}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.AnyOtherIncomeFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.AnyOtherIncomePage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.AnyOtherIncomeView

import scala.concurrent.Future

class AnyOtherIncomeControllerSpec extends SpecBase with MockitoSugar {

  val formProvider           = new AnyOtherIncomeFormProvider()
  val otherIncomeAmountCall  = OtherIncomeAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
  val turnoverNotTaxableCall = TurnoverNotTaxableController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
  val tradingAllowanceCall   = TradingAllowanceController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)

  val mockService: SelfEmploymentService       = mock[SelfEmploymentService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  def redirectCallFromNoResponse(accountingType: String) = accountingType match {
    case `accrual` => turnoverNotTaxableCall
    case `cash`    => tradingAllowanceCall
  }

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider(individual)),
    UserScenario(isWelsh = false, isAgent = true, formProvider(agent))
  )

  "AnyOtherIncome Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when language is ${getLanguage(userScenario.isWelsh)} and user is an ${authUserType(userScenario.isAgent)}" - {
          "must return OK and the correct view for a GET" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, AnyOtherIncomeController.onPageLoad(taxYear, stubbedBusinessId, NormalMode).url)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val expectedResult =
                view(userScenario.form, NormalMode, authUserType(userScenario.isAgent), taxYear, stubbedBusinessId)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, true, Some(stubbedBusinessId)).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, AnyOtherIncomeController.onPageLoad(taxYear, stubbedBusinessId, CheckMode).url)

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(userScenario.form.fill(true), CheckMode, authUserType(userScenario.isAgent), taxYear, stubbedBusinessId)(
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
          val request = FakeRequest(GET, AnyOtherIncomeController.onPageLoad(taxYear, stubbedBusinessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when a user answer 'Yes' is submitted, must redirect to the Other Income Amount page" - {
        "when journey is ACCRUAL accounting type" in {

          val userAnswer = true

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(otherIncomeAmountCall)),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

            val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual otherIncomeAmountCall.url
          }
        }
        "when journey is CASH accounting type" in {

          val userAnswer = true

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(otherIncomeAmountCall)),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right("CASH"))

            val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual otherIncomeAmountCall.url
          }
        }
      }

      "when a user answer 'No' is submitted, must redirect to" - {
        "the Turnover Not Taxable page when journey is ACCRUAL accounting type" in {

          val userAnswer     = false
          val accountingType = "ACCRUAL"

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(redirectCallFromNoResponse(accountingType))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right(accountingType))

            val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual turnoverNotTaxableCall.url
          }
        }
        "the Trading Allowance page when journey is CASH accounting type" in {

          val userAnswer     = false
          val accountingType = "CASH"

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(redirectCallFromNoResponse(accountingType))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right("CASH"))

            val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceCall.url
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when language is ${getLanguage(userScenario.isWelsh)} and user is an ${authUserType(userScenario.isAgent)}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

              val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, stubbedBusinessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

              val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", "non-Boolean"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-Boolean"))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, stubbedBusinessId)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, AnyOtherIncomeController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
