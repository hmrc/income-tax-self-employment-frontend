/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.expenses.tailoring.individualCategories.ProfessionalServiceExpensesFormProvider
import models.NormalMode
import models.common.UserType.{Agent, Individual}
import models.common.{AccountingType, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.individualCategories.ProfessionalServiceExpensesPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import views.html.journeys.expenses.tailoring.individualCategories.ProfessionalServiceExpensesView

import scala.concurrent.Future

class ProfessionalServiceExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val professionalServiceExpensesRoute =
    controllers.journeys.expenses.tailoring.individualCategories.routes.ProfessionalServiceExpensesController
      .onPageLoad(taxYear, businessId, NormalMode)
      .url

  val formProvider = new ProfessionalServiceExpensesFormProvider()

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(userType: UserType, form: Form[Set[ProfessionalServiceExpenses]], accountingType: AccountingType)

  val userScenarios = Seq(
    UserScenario(userType = Individual, formProvider(Individual), AccountingType.Accrual),
    UserScenario(userType = Agent, formProvider(Agent), AccountingType.Cash)
  )

  "ProfessionalServiceExpenses Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, professionalServiceExpensesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

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
              UserAnswers(userAnswersId)
                .set(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.values.toSet, Some(businessId))
                .success
                .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, professionalServiceExpensesRoute)

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

              val result = route(application, request).value

              val expectedResult = view(
                userScenario.form.fill(ProfessionalServiceExpenses.values.toSet),
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
          val request = FakeRequest(GET, professionalServiceExpensesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(AccountingType.Accrual))
          when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(emptyUserAnswers)

          val request =
            FakeRequest(POST, professionalServiceExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", ProfessionalServiceExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            running(application) {
              when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, professionalServiceExpensesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

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
            FakeRequest(POST, professionalServiceExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", ProfessionalServiceExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
