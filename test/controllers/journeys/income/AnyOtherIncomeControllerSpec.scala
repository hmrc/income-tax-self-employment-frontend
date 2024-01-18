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
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import controllers.journeys.income
import controllers.standard
import forms.income.AnyOtherIncomeFormProvider
import models.common.AccountingType.Accrual
import models.common.{AccountingType, BusinessId, UserType}
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import org.mockito.matchers.MacroBasedMatchers
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{AnyOtherIncomePage, OtherIncomeAmountPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.AnyOtherIncomeView

import scala.concurrent.Future

class AnyOtherIncomeControllerSpec extends SpecBase with MockitoSugar with MacroBasedMatchers {

  val formProvider           = new AnyOtherIncomeFormProvider()
  val otherIncomeAmountCall  = income.routes.OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
  val turnoverNotTaxableCall = income.routes.TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
  val tradingAllowanceCall   = income.routes.TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  val incomeCyaCall          = income.routes.IncomeCYAController.onPageLoad(taxYear, businessId)

  val mockService: SelfEmploymentService       = mock[SelfEmploymentService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  case class UserScenario(userType: UserType, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(userType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(userType = UserType.Agent, formProvider(UserType.Agent))
  )

  // TODO Clean these tests up, overly convoluted.
  "AnyOtherIncome Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, income.routes.AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, true, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, income.routes.AnyOtherIncomeController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[AnyOtherIncomeView]

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
          val request = FakeRequest(GET, income.routes.AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when a user answer 'Yes' is submitted, must redirect to the Other Income Amount page in NormalMode or CheckMode" - {
        "when journey is ACCRUAL accounting type" in {

          val userAnswer = true

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(otherIncomeAmountCall)),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            mockService.getAccountingType(*, *[BusinessId], *)(*).value returns Accrual.asRight.asFuture
            mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(emptyUserAnswers)

            val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
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
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            mockService.getAccountingType(*, *[BusinessId], *)(*) returns EitherT(AccountingType.Cash.asRight.asFuture)
            mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(emptyUserAnswers)

            val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, CheckMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual otherIncomeAmountCall.url
          }
        }
      }

      "when a user answer 'No' is submitted, must clear OtherIncomeAmount data and redirect to" - {
        val userAnswer  = false
        val userAnswers = UserAnswers(userAnswersId).set(OtherIncomeAmountPage, BigDecimal(400), Some(businessId)).success.value

        "the Turnover Not Taxable page when journey is ACCRUAL accounting type" in {

          val accountingType = AccountingType.Accrual

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(turnoverNotTaxableCall)),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accountingType))

            val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual turnoverNotTaxableCall.url
            UserAnswers(userAnswersId).get(OtherIncomeAmountPage, Some(businessId)) mustBe None
          }
        }
        "the Trading Allowance page when journey is CASH accounting type" in {

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(tradingAllowanceCall)),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(AccountingType.Cash))

            val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceCall.url
            UserAnswers(userAnswersId).get(OtherIncomeAmountPage, Some(businessId)) mustBe None
          }
        }
        "the CYA page when in CheckMode and income model is now completed" in {

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(incomeCyaCall)),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(AccountingType.Cash))

            val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, CheckMode).url)
              .withFormUrlEncodedBody(("value", userAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual incomeCyaCall.url
            UserAnswers(userAnswersId).get(OtherIncomeAmountPage, Some(businessId)) mustBe None
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(AccountingType.Accrual))

              val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(AccountingType.Accrual))

              val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", "non-Boolean"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-Boolean"))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

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
          val request = FakeRequest(POST, income.routes.AnyOtherIncomeController.onSubmit(taxYear, businessId, NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
