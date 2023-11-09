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
import controllers.journeys.expenses.routes.OfficeSuppliesAmountController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.OfficeSuppliesAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.OfficeSuppliesAmountPage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.OfficeSuppliesAmountView

import scala.concurrent.Future

class OfficeSuppliesAmountControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new OfficeSuppliesAmountFormProvider()

  private val validAnswer                            = BigDecimal.decimal(100.00)
  private val onwardRoute                            = Call("GET", "/foo")
  private lazy val officeSuppliesAmountPageLoadRoute = OfficeSuppliesAmountController.onPageLoad(NormalMode).url
  private lazy val officeSuppliesAmountOnSubmitRoute = OfficeSuppliesAmountController.onSubmit(NormalMode).url
  private val mockSessionRepository                  = mock[SessionRepository]

  case class UserScenario(isWelsh: Boolean, authUser: UserType, form: Form[BigDecimal])

  private val userScenarios = Seq(
    UserScenario(isWelsh = false, authUser = UserType.Individual, formProvider(individual)),
    UserScenario(isWelsh = false, authUser = UserType.Agent, formProvider(agent))
  )

  "OfficeSuppliesAmountController" - {
    userScenarios.foreach { userScenario =>
      s"when language is ${getLanguage(userScenario.isWelsh)}, user is an ${userScenario.authUser}" - {
        val isAgent = userScenario.authUser match {
          case UserType.Individual => false
          case UserType.Agent      => true
        }
        "when loading a page" - {
          "must return OK and the correct view for a GET" in {
            val application = applicationBuilder(Some(emptyUserAnswers), isAgent).build()

            implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
            val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]

            running(application) {
              val request    = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
              val result     = route(application, request).value
              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              status(langResult) mustEqual OK
              contentAsString(langResult) mustEqual view(userScenario.form, NormalMode)(request, messages(application)).toString
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {
            val userAnswers = UserAnswers(userAnswersId).set(OfficeSuppliesAmountPage, validAnswer).success.value

            val application = applicationBuilder(Some(userAnswers), isAgent).build()

            implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
            val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]

            running(application) {
              val request    = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
              val result     = route(application, request).value
              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              status(langResult) mustEqual OK
              contentAsString(langResult) mustEqual view(userScenario.form.fill(validAnswer), NormalMode)(request, messages(application)).toString
            }
          }
          "must redirect to Journey Recovery for a GET if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None, isAgent).build()

            running(application) {
              val request = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
            }
          }
        }

        "when submitting a page" - {
          "must redirect to the next page when valid data is submitted" in {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent)
                .overrides(
                  bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, officeSuppliesAmountOnSubmitRoute)
                  .withFormUrlEncodedBody(("value", validAnswer.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent).build()

            val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]
            implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request =
                FakeRequest(POST, officeSuppliesAmountOnSubmitRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val result     = route(application, request).value
              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              status(langResult) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery for a POST if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None, isAgent).build()

            running(application) {
              val request =
                FakeRequest(POST, officeSuppliesAmountOnSubmitRoute)
                  .withFormUrlEncodedBody(("value", validAnswer.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }

}
