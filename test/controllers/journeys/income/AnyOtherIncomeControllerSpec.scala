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
import controllers.journeys.income.routes.AnyOtherIncomeController
import controllers.standard.routes.JourneyRecoveryController
import forms.income.AnyOtherIncomeFormProvider
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.AnyOtherIncomePage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.AnyOtherIncomeView

import scala.concurrent.Future

class AnyOtherIncomeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider       = new AnyOtherIncomeFormProvider()
  val formWithIndividual = formProvider("individual")
  val formWithAgent      = formProvider("agent")

  def anyOtherIncomeRoute(isPost: Boolean, mode: Mode): String =
    if (isPost) AnyOtherIncomeController.onSubmit(taxYear, mode).url
    else AnyOtherIncomeController.onPageLoad(taxYear, mode).url

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formWithIndividual),
    UserScenario(isWelsh = false, isAgent = true, formWithAgent)
  )

  "AnyOtherIncome Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when language is ${isWelshToString(userScenario.isWelsh)} and user is an ${isAgentToString(userScenario.isAgent)}" - {
          "must return OK and the correct view for a GET" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(GET, anyOtherIncomeRoute(false, NormalMode), userScenario.isAgent)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val expectedResult =
                view(userScenario.form, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, true).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(GET, anyOtherIncomeRoute(false, CheckMode), userScenario.isAgent)

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(userScenario.form.fill(true), CheckMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(GET, anyOtherIncomeRoute(false, NormalMode), false)

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
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request = buildRequest(POST, anyOtherIncomeRoute(true, NormalMode), false)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when language is ${isWelshToString(userScenario.isWelsh)} and user is an ${isAgentToString(userScenario.isAgent)}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(POST, anyOtherIncomeRoute(true, NormalMode), userScenario.isAgent)
                .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
          "must return a Bad Request and errors when invalid data is submitted" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(POST, anyOtherIncomeRoute(true, NormalMode), userScenario.isAgent)
                .withFormUrlEncodedBody(("value", "non-Boolean"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-Boolean"))

              val view = application.injector.instanceOf[AnyOtherIncomeView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(POST, anyOtherIncomeRoute(true, NormalMode), false)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
