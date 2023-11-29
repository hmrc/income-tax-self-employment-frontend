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

package controllers.journeys.expenses.goodsToSellOrUse

import base.SpecBase
import controllers.journeys.expenses.goodsToSellOrUse.routes.GoodsToSellOrUseAmountController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountFormProvider
import models.database.UserAnswers
import models.journeys.expenses.TaxiMinicabOrRoadHaulage
import models.journeys.expenses.TaxiMinicabOrRoadHaulage.{No, Yes}
import models.{CheckMode, NormalMode}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountPage
import pages.expenses.tailoring.TaxiMinicabOrRoadHaulagePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountView

import scala.concurrent.Future

class GoodsToSellOrUseAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new GoodsToSellOrUseAmountFormProvider()
  val validAnswer: BigDecimal = 10
  val onwardRoute             = Call("GET", "/foo")

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[BigDecimal], accountingType: String, taxiDriver: TaxiMinicabOrRoadHaulage)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider(individual), accrual, taxiDriver = Yes),
    UserScenario(isWelsh = false, isAgent = true, formProvider(agent), cash, taxiDriver = No)
  )

  "GoodsToSellOrUseAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${userType(userScenario.isAgent)}, ${userScenario.accountingType} and is ${if (!userScenario.taxiDriver.equals(Yes)) "not "
          else ""}a taxi driver" - {
          "must return OK and the correct view for a GET" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value
            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val expectedResult =
                view(
                  userScenario.form,
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes)
                )(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(GoodsToSellOrUseAmountPage, validAnswer, Some(businessId))
              .success
              .value
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, CheckMode).url)

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  userScenario.form.fill(validAnswer),
                  CheckMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes)
                )(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None)
          .build()

        running(application) {
          val request = FakeRequest(GET, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)

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
              bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

          val request =
            FakeRequest(POST, GoodsToSellOrUseAmountController.onSubmit(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, ${userType(userScenario.isAgent)}, ${userScenario.accountingType} and is ${if (!userScenario.taxiDriver.equals(Yes)) "not "
          else ""}a taxi driver" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value
            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  boundForm,
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes))(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value
            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  boundForm,
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes))(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when a zero or negative number is submitted" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value
            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "0"))

              val boundForm = userScenario.form.bind(Map("value" -> "0"))

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  boundForm,
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes))(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when amount exceeds the maximum" in {

            val userAnswers = UserAnswers(userAnswersId)
              .set(TaxiMinicabOrRoadHaulagePage, userScenario.taxiDriver, Some(businessId))
              .success
              .value
            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)
                  .withFormUrlEncodedBody(("value", "1006454566540"))

              val boundForm = userScenario.form.bind(Map("value" -> "1006454566540"))

              val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  boundForm,
                  NormalMode,
                  userType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType,
                  userScenario.taxiDriver.equals(Yes))(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None)
          .build()

        running(application) {
          val request =
            FakeRequest(POST, GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
