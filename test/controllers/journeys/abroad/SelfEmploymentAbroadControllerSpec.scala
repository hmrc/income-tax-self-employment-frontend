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

package controllers.journeys.abroad

import base.SpecBase
import forms.abroad.SelfEmploymentAbroadFormProvider
import models.journeys.Abroad
import models.NormalMode
import models.database.UserAnswers
import navigation.{FakeAbroadNavigator, AbroadNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.abroad.SelfEmploymentAbroadPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.abroad.SelfEmploymentAbroadView

import scala.concurrent.Future

class SelfEmploymentAbroadControllerSpec extends SpecBase with MockitoSugar {

  val isAgent             = false
  val formProvider        = new SelfEmploymentAbroadFormProvider()
  val form: Form[Boolean] = formProvider(isAgent)
  val businessId          = "businessId-1"

  lazy val selfEmploymentAbroadRoute: String = routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val taskListRoute: String             = controllers.journeys.routes.TaskListController.onPageLoad(taxYear).url
  lazy val taskListCall: Call                = Call("GET", taskListRoute)
  lazy val journeyRecoveryRoute: String      = controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
  lazy val journeyRecoveryCall: Call         = Call("GET", journeyRecoveryRoute)

  lazy val sectionCompletedStateRoute: String =
    controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, Abroad.toString, NormalMode).url

  lazy val sectionCompletedStateCall: Call = Call("GET", journeyRecoveryRoute)

  "SelfEmploymentAbroad Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, selfEmploymentAbroadRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentAbroadView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, businessId, isAgent, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly for a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers(userAnswersId).set(SelfEmploymentAbroadPage, true, Some(businessId)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, selfEmploymentAbroadRoute)

          val view = application.injector.instanceOf[SelfEmploymentAbroadView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), taxYear, businessId, isAgent, NormalMode)(request, messages(application)).toString
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
              bind[AbroadNavigator].toInstance(new FakeAbroadNavigator(sectionCompletedStateCall)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, selfEmploymentAbroadRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual sectionCompletedStateCall.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, selfEmploymentAbroadRoute)
              .withFormUrlEncodedBody(("value", "OhDear"))

          val boundForm = form.bind(Map("value" -> "OhDear"))

          val view = application.injector.instanceOf[SelfEmploymentAbroadView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, businessId, isAgent, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to the Journey Recovery page when session repository fails to set new data" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(false)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, selfEmploymentAbroadRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecoveryCall.url
        }
      }

    }
  }

}
