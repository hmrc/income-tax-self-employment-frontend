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
import controllers.journeys.income.routes.HowMuchTradingAllowanceController
import controllers.standard.routes.JourneyRecoveryController
import forms.income.HowMuchTradingAllowanceFormProvider
import models.{HowMuchTradingAllowance, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.HowMuchTradingAllowancePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.income.HowMuchTradingAllowanceView

import scala.concurrent.Future

class HowMuchTradingAllowanceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                = new HowMuchTradingAllowanceFormProvider()
  val maxTradingAllowanceString   = (1000.00).toString
  val smallTradingAllowanceString = (260.50).toString
  val formWithMaxTA               = formProvider(isAgentString, maxTradingAllowanceString)
  val formWithSmallTA             = formProvider(isAgentString, smallTradingAllowanceString)
  val isAgentString               = "individual"
  val taxYear                     = LocalDate.now().getYear

  lazy val howMuchTradingAllowanceRoute = HowMuchTradingAllowanceController.onPageLoad(taxYear, NormalMode).url

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[HowMuchTradingAllowance])

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = true, form = formWithMaxTA),
    UserScenario(isWelsh = true, isAgent = false, form = formWithSmallTA)
  )

  def formTest(form: Form[HowMuchTradingAllowance]): String = if (form.equals(formWithMaxTA)) "max allowance" else "non-max allowance"

  "HowMuchTradingAllowance Controller" - {

    "onPageLoad" - {

      //      userScenarios.foreach { userScenario =>
      //        s"when ${welshTest(userScenario.isWelsh)}, ${agentTest(userScenario.isAgent)} and using the ${formTest(userScenario.form)}" - {
      "must return OK with the correct view" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, howMuchTradingAllowanceRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(formWithMaxTA, NormalMode, isAgentString, taxYear, maxTradingAllowanceString)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(HowMuchTradingAllowancePage, HowMuchTradingAllowance.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, howMuchTradingAllowanceRoute)

          val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(formWithSmallTA.fill(HowMuchTradingAllowance.values.head), NormalMode, isAgentString, taxYear, smallTradingAllowanceString)(
              request,
              messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, howMuchTradingAllowanceRoute)

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
          val request =
            FakeRequest(POST, howMuchTradingAllowanceRoute)
              .withFormUrlEncodedBody(("value", HowMuchTradingAllowance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, howMuchTradingAllowanceRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = formWithSmallTA.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[HowMuchTradingAllowanceView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, isAgentString, taxYear, smallTradingAllowanceString)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, howMuchTradingAllowanceRoute)
              .withFormUrlEncodedBody(("value", HowMuchTradingAllowance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
