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

package controllers.journeys.expenses.tailoring

import base.SpecBase
import controllers.journeys.expenses.tailoring.routes.WorkFromBusinessPremisesController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.tailoring.WorkFromBusinessPremisesFormProvider
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.WorkFromBusinessPremises
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.WorkFromBusinessPremisesPage
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.ContentStringViewModel.buildLegendHeadingWithHintString
import views.html.journeys.expenses.tailoring.WorkFromBusinessPremisesView

import scala.concurrent.Future

class WorkFromBusinessPremisesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val workFromBusinessPremisesRoute = WorkFromBusinessPremisesController.onPageLoad(NormalMode).url

  val formProvider = new WorkFromBusinessPremisesFormProvider()

  def buildLegendContent(userType: String)(implicit messages: Messages) = buildLegendHeadingWithHintString(
    s"workFromBusinessPremises.title.$userType",
    s"workFromBusinessPremises.hint.$userType",
    headingClasses = "govuk-fieldset__legend govuk-fieldset__legend--l"
  )

  case class UserScenario(isWelsh: Boolean, authUserType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, authUserType = individual),
    UserScenario(isWelsh = false, authUserType = agent)
  )

  "WorkFromBusinessPremises Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when language is ${getLanguage(userScenario.isWelsh)} and user is an ${userScenario.authUserType}" - {

          "must return OK and the correct view for a GET" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.authUserType)).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]
            val form                 = formProvider(userScenario.authUserType)
            val legendContent        = buildLegendContent(userScenario.authUserType)(messages(application))

            running(application) {
              val request = FakeRequest(GET, workFromBusinessPremisesRoute)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val expectedResult =
                view(form, NormalMode, userScenario.authUserType, taxYear, businessId, legendContent)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.values.head, Some(businessId)).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), isAgent(userScenario.authUserType)).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]
            val form                 = formProvider(userScenario.authUserType)
            val legendContent        = buildLegendContent(userScenario.authUserType)(messages(application))

            running(application) {
              val request = FakeRequest(GET, workFromBusinessPremisesRoute)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val expectedResult =
                view(form.fill(WorkFromBusinessPremises.values.head), NormalMode, userScenario.authUserType, taxYear, businessId, legendContent)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, workFromBusinessPremisesRoute)

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
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, workFromBusinessPremisesRoute)
              .withFormUrlEncodedBody(("value", WorkFromBusinessPremises.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when language is ${getLanguage(userScenario.isWelsh)} and user is an ${userScenario.authUserType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.authUserType)).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]
            val form                 = formProvider(userScenario.authUserType)
            val legendContent        = buildLegendContent(userScenario.authUserType)(messages(application))

            running(application) {
              val request =
                FakeRequest(POST, workFromBusinessPremisesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId, legendContent)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.authUserType)).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]
            val form                 = formProvider(userScenario.authUserType)
            val legendContent        = buildLegendContent(userScenario.authUserType)(messages(application))

            running(application) {
              val request =
                FakeRequest(POST, workFromBusinessPremisesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult =
                view(boundForm, NormalMode, userScenario.authUserType, taxYear, businessId, legendContent)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, workFromBusinessPremisesRoute)
              .withFormUrlEncodedBody(("value", WorkFromBusinessPremises.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
