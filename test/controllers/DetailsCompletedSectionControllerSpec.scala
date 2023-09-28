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

package controllers

import base.SpecBase
import connectors.SelfEmploymentConnector
import forms.DetailsCompletedSectionFormProvider
import models.errors.{HttpError, HttpErrorBody}
import models.{DetailsCompletedSection, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.DetailsCompletedSectionPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.JourneyStateService
import views.html.DetailsCompletedSectionView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DetailsCompletedSectionControllerSpec extends SpecBase with MockitoSugar {

  val taxYear: Int = LocalDate.now().getYear
  val nino = "AA112233A"
  val journey = "journeyId"
  val businessId = journey + "-" + nino
  val mtditid = "mtditid"

  val mockService: JourneyStateService = mock[JourneyStateService]
  implicit val ec: ExecutionContext = ExecutionContext.global
  val formProvider = new DetailsCompletedSectionFormProvider()
  val form: Form[DetailsCompletedSection] = formProvider()

  lazy val detailsCompletedSectionRoute: String = routes.DetailsCompletedSectionController.onPageLoad(
    taxYear, journey, NormalMode).url
  lazy val journeyRecoveryRoute: String = routes.JourneyRecoveryController.onPageLoad().url
  lazy val journeyRecoveryCall: Call = Call("GET", journeyRecoveryRoute)
  lazy val taskListRoute: String = routes.TaskListController.onPageLoad.url
  lazy val taskListCall: Call = Call("GET", taskListRoute)

  "DetailsCompletedSection Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[JourneyStateService].toInstance(mockService)).build()

        running(application) {

          when(mockService.getJourneyState(any, meq(journey), meq(taxYear), any)(any)) thenReturn Future(Right(None))

          val request = FakeRequest(GET, detailsCompletedSectionRoute)

          val view = application.injector.instanceOf[DetailsCompletedSectionView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, taxYear, journey, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[JourneyStateService].toInstance(mockService)).build()

        running(application) {
          when(mockService.getJourneyState(any, meq(journey), meq(taxYear), any)(any)) thenReturn Future(Right(Some(true)))

          val request = FakeRequest(GET, detailsCompletedSectionRoute)

          val view = application.injector.instanceOf[DetailsCompletedSectionView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            form.fill(DetailsCompletedSection.values.head), taxYear, journey, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        when(mockService.saveJourneyState(any, meq(journey), meq(taxYear), complete = meq(true), any)(any)
        ) thenReturn Future(Right(None))

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[JourneyStateService].toInstance(mockService)).build()

        running(application) {
          val request =
            FakeRequest(POST, detailsCompletedSectionRoute)
              .withFormUrlEncodedBody(("value", DetailsCompletedSection.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual taskListRoute
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        when(mockService.saveJourneyState(any, meq(journey), meq(taxYear), complete = meq(true), any)(any)
        ) thenReturn Future(Right(None))

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[JourneyStateService].toInstance(mockService)).build()

        running(application) {
          val request =
            FakeRequest(POST, detailsCompletedSectionRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[DetailsCompletedSectionView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, taxYear, journey, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when an error response is returned from the service" in {

        when(mockService.saveJourneyState(any, meq("invalidJourneyId"), meq(taxYear),
          complete = meq(true), any)(any)
        ) thenReturn Future(Left(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("400", "Error"))))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(journeyRecoveryCall)),
              bind[JourneyStateService].toInstance(mockService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, detailsCompletedSectionRoute)
            .withFormUrlEncodedBody(("value", DetailsCompletedSection.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecoveryRoute
        }
      }
    }
  }
}
