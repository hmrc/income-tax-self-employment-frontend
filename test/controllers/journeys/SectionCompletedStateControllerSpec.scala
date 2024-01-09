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

package controllers.journeys

import base.SpecBase
import forms.SectionCompletedStateFormProvider
import models.NormalMode
import models.common.JourneyStatus
import models.journeys.{CompletedSectionState, Journey}
import navigation._
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.services.SelfEmploymentServiceStub
import views.html.journeys.SectionCompletedStateView
import services.SelfEmploymentService

class SectionCompletedStateControllerSpec extends SpecBase with TableDrivenPropertyChecks {
  val journey                           = Journey.Abroad
  val stubService                       = SelfEmploymentServiceStub()
  val formProvider                      = new SectionCompletedStateFormProvider()
  val form: Form[CompletedSectionState] = formProvider()

  lazy val sectionCompletedStateRoute: String =
    routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, journey.entryName, NormalMode).url
  lazy val journeyRecoveryRoute: String = controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
  lazy val journeyRecoveryCall: Call    = Call("GET", journeyRecoveryRoute)
  lazy val taskListRoute: String        = routes.TaskListController.onPageLoad(taxYear).url
  lazy val taskListCall: Call           = Call("GET", taskListRoute)

  "SectionCompletedStateController" - {

    "onPageLoad" - {
      val cases = Table(
        ("status", "expectedForm"),
        (JourneyStatus.CheckOurRecords, form),
        (JourneyStatus.CannotStartYet, form),
        (JourneyStatus.NotStarted, form),
        (JourneyStatus.InProgress, form.fill(CompletedSectionState.No)),
        (JourneyStatus.Completed, form.fill(CompletedSectionState.Yes))
      )

      forAll(cases) { case (journeyStatus, expectedForm) =>
        s"must return OK and the correct view for a GET when status=${journeyStatus.entryName}" in {
          val application = createApp(
            stubService.copy(
              getJourneyStatusResult = Right(journeyStatus)
            ))

          running(application) {
            val request = FakeRequest(GET, sectionCompletedStateRoute)
            val view    = application.injector.instanceOf[SectionCompletedStateView]
            val result  = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(expectedForm, taxYear, businessId, journey, NormalMode)(request, messages(application)).toString
          }
        }
      }
    }

    "onSubmit" - {
      "must redirect to the next page when valid data is submitted" in {
        val application = createApp(stubService)

        running(application) {
          val request =
            FakeRequest(POST, sectionCompletedStateRoute)
              .withFormUrlEncodedBody(("value", CompletedSectionState.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual taskListRoute
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = createApp(stubService)

        running(application) {
          val request =
            FakeRequest(POST, sectionCompletedStateRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[SectionCompletedStateView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, businessId, journey, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when an error response is returned from the service" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[GeneralNavigator].toInstance(new FakeGeneralNavigator(journeyRecoveryCall)),
              bind[SelfEmploymentService].toInstance(stubService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, sectionCompletedStateRoute)
            .withFormUrlEncodedBody(("value", CompletedSectionState.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecoveryRoute
        }
      }
    }
  }
}
