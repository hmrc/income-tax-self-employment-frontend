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
import controllers.journeys.income
import controllers.standard
import forms.income.OtherIncomeAmountFormProvider
import models.common._
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.matchers.MacroBasedMatchers
import org.scalatestplus.mockito.MockitoSugar
import pages.income.OtherIncomeAmountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import views.html.journeys.income.OtherIncomeAmountView

import scala.concurrent.Future

class OtherIncomeAmountControllerSpec extends SpecBase with MockitoSugar with MacroBasedMatchers {

  val formProvider            = new OtherIncomeAmountFormProvider()
  val validAnswer: BigDecimal = 100.00
  val turnoverAmount          = 1000.00
  val turnoverNotTaxableCall  = income.routes.TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
  val tradingAllowanceCall    = income.routes.TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  val cyaCall                 = income.routes.IncomeCYAController.onPageLoad(taxYear, businessId)

  val mockService = mock[SelfEmploymentService]

  def onwardRoute(accountingType: AccountingType): Call =
    if (accountingType == AccountingType.Accrual) turnoverNotTaxableCall else tradingAllowanceCall

  case class UserScenario(userType: UserType, form: Form[BigDecimal])

  val userScenarios = Seq(
    UserScenario(userType = UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(userType = UserType.Agent, formProvider(UserType.Agent))
  )
  // TODO Clean these tests up, overly convoluted.
  "OtherIncomeAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, income.routes.OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(OtherIncomeAmountPage, validAnswer, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, income.routes.OtherIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val expectedResult = view(userScenario.form.fill(validAnswer), CheckMode, userScenario.userType, taxYear, businessId)(
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
          val request = FakeRequest(GET, income.routes.OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when valid data is submitted must redirect to the" - {
        "TurnoverNotTaxable page when accounting type is 'ACCRUAL'" in {

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(AccountingType.Accrual))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Accrual)
            mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(emptyUserAnswers)

            val request =
              FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual turnoverNotTaxableCall.url
          }
        }

        "TradingAllowance page when accounting type is 'CASH'" in {

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute(AccountingType.Cash))),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Cash)
            mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(emptyUserAnswers)

            val request =
              FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceCall.url
          }
        }

        "CYA page when in CheckMode and income model is now completed" in {

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(cyaCall)),
                bind[SelfEmploymentService].toInstance(mockService)
              )
              .build()

          running(application) {
            mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Cash)

            val request =
              FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, CheckMode).url)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual cyaCall.url
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
              mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Accrual)
              val request =
                FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

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
              mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Accrual)
              val request =
                FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "non-BigDecimal"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "a negative number is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Accrual)
              val request =
                FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "-23"))

              val boundForm = userScenario.form.bind(Map("value" -> "-23"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "turnover income amount exceeds £100,000,000,000.00" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(AccountingType.Accrual)
              val request =
                FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "100000000000.01"))

              val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

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
            FakeRequest(POST, income.routes.OtherIncomeAmountController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
