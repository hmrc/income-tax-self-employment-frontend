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

package controllers.journeys.expenses.simplifiedExpenses

import base.SpecBase
import forms.expenses.simplifiedExpenses.TotalExpensesFormProvider
import models.NormalMode
import models.database.UserAnswers
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.simplifiedExpenses.TotalExpensesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.simplifiedExpenses.TotalExpensesView
import controllers.standard.routes.JourneyRecoveryController
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi

import scala.concurrent.Future

class TotalExpensesControllerSpec extends SpecBase with MockitoSugar {

  lazy val totalExpensesRoute = routes.TotalExpensesController.onPageLoad(taxYear, businessId, NormalMode).url
  val formProvider            = new TotalExpensesFormProvider()

  private val businessId  = "some_id"
  private val validAnswer = BigDecimal(100.00)

  private def buildApplication(userAnswers: Option[UserAnswers], authUser: String): Application = {
    val isAgent = authUser match {
      case "individual" => false
      case "agent" => true
    }
    applicationBuilder(userAnswers, isAgent)
      .build()
  }

  private val userScenarios = Seq(
    UserScenario(isWelsh = false, authUser = individual, form = formProvider(individual)),
    UserScenario(isWelsh = true, authUser = agent, form = formProvider(agent))
  )

  def onwardRoute = Call("GET", "/foo")

  case class UserScenario(isWelsh: Boolean, authUser: String, form: Form[BigDecimal])

  "TotalExpenses Controller" - {
    userScenarios.foreach { userScenario =>
      s"when language is ${getLanguage(userScenario.isWelsh)}, user is an ${userScenario.authUser}" - {
        "when loading a page" - {
          "must return OK and the correct view for a GET" in {

            val application                       = buildApplication(Some(emptyUserAnswers), userScenario.authUser)
            val view                              = application.injector.instanceOf[TotalExpensesView]
            implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, totalExpensesRoute)

              val result     = route(application, request).value
              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual view(userScenario.form, NormalMode, userScenario.authUser, taxYear, businessId)(
                request,
                messages(application)).toString
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TotalExpensesPage, validAnswer, Some(businessId)).success.value

            val application                       = buildApplication(Some(userAnswers), userScenario.authUser)
            val view                              = application.injector.instanceOf[TotalExpensesView]
            implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = FakeRequest(GET, totalExpensesRoute)

              val result     = route(application, request).value
              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              status(result) mustEqual OK

              contentAsString(langResult) mustEqual view(userScenario.form.fill(validAnswer), NormalMode, userScenario.authUser, taxYear, businessId)(
                request,
                messages(application)).toString
            }
          }
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, totalExpensesRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when empty data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, totalExpensesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val form      = formProvider(individual)
        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TotalExpensesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, individual, taxYear, businessId)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, totalExpensesRoute)
            .withFormUrlEncodedBody(("value", "invalid"))

        val form      = formProvider(individual)
        val boundForm = form.bind(Map("value" -> "invalid"))

        val view = application.injector.instanceOf[TotalExpensesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, individual, taxYear, businessId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, totalExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = buildApplication(Some(emptyUserAnswers), individual)

      running(application) {
        val request =
          FakeRequest(POST, totalExpensesRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
