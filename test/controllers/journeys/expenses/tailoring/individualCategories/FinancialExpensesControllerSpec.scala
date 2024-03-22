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

package controllers.journeys.expenses.tailoring.individualCategories

import base.SpecBase
import controllers.standard
import forms.expenses.tailoring.individualCategories.FinancialExpensesFormProvider
import models.NormalMode
import models.common.UserType.{Agent, Individual}
import models.common._
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.FinancialExpenses
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.Mockito.when
import org.mockito.matchers.MacroBasedMatchers
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.individualCategories.FinancialExpensesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import views.html.journeys.expenses.tailoring.individualCategories.FinancialExpensesView

import scala.concurrent.Future
import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import forms.expenses.tailoring.individualCategories.GoodsToSellOrUseFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.GoodsToSellOrUse
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.TradeAccountingType
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView


class FinancialExpensesControllerSpec extends SpecBase with MockitoSugar with MacroBasedMatchers {

  def onwardRoute = Call("GET", "/foo")

  lazy val financialExpensesRoute = routes.FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider = new FinancialExpensesFormProvider()

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(userType: UserType, form: Form[Set[FinancialExpenses]], accountingType: AccountingType, baseUserAnswers: UserAnswers)

  val userScenarios = Seq(
    UserScenario(userType = Individual, formProvider(Individual), AccountingType.Accrual, baseUserAnswers = emptyUserAnswersAccrual),
    UserScenario(userType = Agent, formProvider(Agent), AccountingType.Cash, baseUserAnswers = emptyUserAnswersCash)
  )

  "FinancialExpenses Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(userScenario.baseUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {

              val request = FakeRequest(GET, financialExpensesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[FinancialExpensesView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              userScenario.baseUserAnswers.set(FinancialExpensesPage, FinancialExpenses.values.toSet, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {

              val request = FakeRequest(GET, financialExpensesRoute)

              val view = application.injector.instanceOf[FinancialExpensesView]

              val result = route(application, request).value

              val expectedResult =
                view(
                  userScenario.form.fill(FinancialExpenses.values.toSet),
                  NormalMode,
                  userScenario.userType,
                  taxYear,
                  businessId,
                  userScenario.accountingType
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
          val request = FakeRequest(GET, financialExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersAccrual))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService)
            )
            .build()

        running(application) {
          when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(emptyUserAnswers)

          val request =
            FakeRequest(POST, financialExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", FinancialExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(userScenario.baseUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {

              val request =
                FakeRequest(POST, financialExpensesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[FinancialExpensesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(userScenario.baseUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {

              val request =
                FakeRequest(POST, financialExpensesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[FinancialExpensesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType)(
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
          val request =
            FakeRequest(POST, financialExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", FinancialExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
