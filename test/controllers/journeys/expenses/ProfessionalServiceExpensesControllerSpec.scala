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
import forms.expenses.ProfessionalServiceExpensesFormProvider
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.ProfessionalServiceExpenses
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.ProfessionalServiceExpensesPage
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import viewmodels.ContentStringViewModel.buildLegendHeadingWithHintString
import views.html.journeys.expenses.ProfessionalServiceExpensesView

import scala.concurrent.Future

class ProfessionalServiceExpensesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val professionalServiceExpensesRoute = controllers.journeys.expenses.routes.ProfessionalServiceExpensesController.onPageLoad(NormalMode).url

  val formProvider = new ProfessionalServiceExpensesFormProvider()

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  def buildLegendContent(userType: String)(implicit messages: Messages) = buildLegendHeadingWithHintString(
    s"professionalServiceExpenses.subHeading.$userType",
    "site.selectAllThatApply",
    headingClasses = "govuk-fieldset__legend govuk-fieldset__legend--m"
  )

  case class UserScenario(isWelsh: Boolean, userType: String, form: Form[Set[ProfessionalServiceExpenses]], accountingType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, userType = individual, formProvider(individual), accrual),
    UserScenario(isWelsh = false, userType = agent, formProvider(agent), cash)
  )

  "ProfessionalServiceExpenses Controller" - {

    "onPageLoad" - {
      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()

            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {

              val request = FakeRequest(GET, professionalServiceExpensesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, legendContent)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId)
                .set(ProfessionalServiceExpensesPage, ProfessionalServiceExpenses.values.toSet, Some(stubbedBusinessId))
                .success
                .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              val request = FakeRequest(GET, professionalServiceExpensesRoute)

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

              val result = route(application, request).value

              val expectedResult = view(
                userScenario.form.fill(ProfessionalServiceExpenses.values.toSet),
                NormalMode,
                userScenario.userType,
                legendContent)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

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

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, professionalServiceExpensesRoute)
              .withFormUrlEncodedBody(("value[0]", ProfessionalServiceExpenses.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${userScenario.userType} and using ${userScenario.accountingType} accounting type" - {

          //        "must return a Bad Request and errors when an empty form is submitted" in {

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.userType))
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            val legendContent = buildLegendContent(userScenario.userType)(messages(application))

            running(application) {
              val request =
                FakeRequest(POST, professionalServiceExpensesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[ProfessionalServiceExpensesView]

              val result = route(application, request).value

              val expectedResult = view(boundForm, NormalMode, userScenario.userType, legendContent)(request, messages(application)).toString

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
