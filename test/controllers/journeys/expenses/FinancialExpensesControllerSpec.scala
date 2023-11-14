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

package controllers.journeys.expenses

import base.SpecBase
import controllers.journeys.expenses.routes.FinancialExpensesController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.FinancialExpensesFormProvider
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.FinancialExpenses
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.FinancialExpensesPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import viewmodels.ContentStringViewModel.buildLegendHeadingWithHintString
import views.html.journeys.expenses.FinancialExpensesView

import scala.concurrent.Future

class FinancialExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val financialExpensesRoute = FinancialExpensesController.onPageLoad(NormalMode).url

  val formProvider = new FinancialExpensesFormProvider()

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  def buildLegendContent(userType: String)(implicit messages: Messages) = buildLegendHeadingWithHintString(
    s"financialExpenses.subHeading.$userType",
    "site.selectAllThatApply",
    headingClasses = "govuk-fieldset__legend govuk-fieldset__legend--m"
  )

  case class UserScenario(isWelsh: Boolean, userType: String, form: Form[Set[FinancialExpenses]], accountingType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, userType = individual, formProvider(individual), accrual),
    UserScenario(isWelsh = false, userType = agent, formProvider(agent), cash)
  )

  "FinancialExpenses Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, financialExpensesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[FinancialExpensesView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType, legendContent)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(FinancialExpensesPage, FinancialExpenses.values.toSet, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

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
                  userScenario.accountingType,
                  legendContent
                )(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, financialExpensesRoute)

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
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(accrual))

          val request =
            FakeRequest(POST, financialExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", FinancialExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, financialExpensesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[FinancialExpensesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType, legendContent)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, financialExpensesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[FinancialExpensesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId, userScenario.accountingType, legendContent)(
                  request,
                  messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, financialExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", FinancialExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
