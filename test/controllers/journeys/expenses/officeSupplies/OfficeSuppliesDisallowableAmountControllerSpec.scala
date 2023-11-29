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

package controllers.journeys.expenses.officeSupplies

import base.SpecBase
import controllers.journeys.expenses.officeSupplies.routes.OfficeSuppliesDisallowableAmountController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.NormalMode
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.officeSupplies.OfficeSuppliesDisallowableAmountPage
import play.api.i18n._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.MoneyUtils
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesDisallowableAmountView

import scala.concurrent.Future

class OfficeSuppliesDisallowableAmountControllerSpec extends SpecBase with MockitoSugar with MoneyUtils {

  private val formProvider = new OfficeSuppliesDisallowableAmountFormProvider()

  private val validAnswer     = BigDecimal(1000.00)
  private val allowableAmount = BigDecimal(1000.00)

  private val onwardRoute = Call("GET", "/foo")

  private lazy val officeSuppliesDisallowableAmountPageLoadRoute =
    OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url

  private lazy val officeSuppliesDisallowableAmountOnSubmitRoute =
    OfficeSuppliesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  private val mockSessionRepository = mock[SessionRepository]

  case class UserScenario(authUser: String)

  private val userScenarios = Seq(UserScenario(authUser = individual), UserScenario(authUser = agent))

  private val data        = Json.obj(businessId.value -> Json.obj("officeSuppliesAmount" -> allowableAmount))
  private val userAnswers = UserAnswers(userAnswersId, data)

  "OfficeSuppliesDisallowableAmountController" - {
    userScenarios.foreach { userScenario =>
      s"when user is an ${userScenario.authUser}" - {
        "when loading a page" - {
          "when office supplies allowable amount has been provided in the previous question" - {
            "must return OK and the correct view" in {
              val application = applicationBuilder(Some(userAnswers), isAgent(userScenario.authUser)).build()

              implicit val appMessages: Messages = messages(application)

              val view: OfficeSuppliesDisallowableAmountView = application.injector.instanceOf[OfficeSuppliesDisallowableAmountView]

              running(application) {
                val request = FakeRequest(GET, officeSuppliesDisallowableAmountPageLoadRoute)
                val result  = route(application, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual view(
                  formProvider(userScenario.authUser, allowableAmount),
                  NormalMode,
                  taxYear,
                  businessId,
                  userScenario.authUser,
                  formatMoney(allowableAmount))(request, appMessages).toString
              }
            }

            "must populate the view correctly when the question has already been answered" in {
              val existingUserAnswers = userAnswers.set(OfficeSuppliesDisallowableAmountPage, validAnswer, Some(businessId)).success.value

              val application = applicationBuilder(Some(existingUserAnswers), isAgent(userScenario.authUser)).build()

              implicit val appMessages: Messages             = messages(application)
              val view: OfficeSuppliesDisallowableAmountView = application.injector.instanceOf[OfficeSuppliesDisallowableAmountView]

              running(application) {
                val request = FakeRequest(GET, officeSuppliesDisallowableAmountPageLoadRoute)
                val result  = route(application, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual view(
                  formProvider(userScenario.authUser, allowableAmount).fill(validAnswer),
                  NormalMode,
                  taxYear,
                  businessId,
                  userScenario.authUser,
                  formatMoney(allowableAmount)
                )(request, appMessages).toString
              }
            }
          }
          "when the allowable amount of office supplies has not been provided" - {
            "must redirect to Journey Recovery if no existing data is found" in {
              val application = applicationBuilder(userAnswers = None, isAgent(userScenario.authUser)).build()

              running(application) {
                val request = FakeRequest(GET, officeSuppliesDisallowableAmountPageLoadRoute)
                val result  = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
              }
            }
          }
        }

        "when submitting a page" - {
          "must redirect to the next page when valid data is submitted" in {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers), isAgent(userScenario.authUser))
                .overrides(
                  bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

            running(application) {
              val request = FakeRequest(POST, officeSuppliesDisallowableAmountOnSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))
              val result  = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {
            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent(userScenario.authUser)).build()

            val view: OfficeSuppliesDisallowableAmountView = application.injector.instanceOf[OfficeSuppliesDisallowableAmountView]
            implicit val appMessages: Messages             = messages(application)

            running(application) {
              val request   = FakeRequest(POST, officeSuppliesDisallowableAmountOnSubmitRoute).withFormUrlEncodedBody(("value", "invalid value"))
              val boundForm = formProvider(userScenario.authUser, allowableAmount).bind(Map("value" -> "invalid value"))
              val result    = route(application, request).value

              status(result) mustEqual BAD_REQUEST

              contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, businessId, userScenario.authUser, formatMoney(allowableAmount))(
                request,
                appMessages).toString
            }
          }

          "must redirect to Journey Recovery if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None, isAgent(userScenario.authUser)).build()

            running(application) {
              val request = FakeRequest(POST, officeSuppliesDisallowableAmountOnSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))
              val result  = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }

}
