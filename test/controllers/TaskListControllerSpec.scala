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
import builders.TradesJourneyStatusesBuilder.aSequenceTadesJourneyStatusesModel
import builders.UserBuilder.aNoddyUser
import common.{apiResultT, leftApiResultT}
import connectors.SelfEmploymentConnector
import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.journeys.routes
import models.common.JourneyStatus
import models.errors.{HttpError, HttpErrorBody}
import models.requests.TradesJourneyStatuses
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.journeys.TaskListView

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase with MockitoSugar with ArgumentMatchersSugar {

  val nino       = "AA370343B"
  val user: User = User(mtditid, None, nino, AffinityGroup.Individual.toString)

  val mockService: SelfEmploymentService     = mock[SelfEmploymentService]
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  "TaskListController .onPageLoad" - {

    "must return OK and display Self-employments when review of trade details has been completed" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SelfEmploymentService].toInstance(mockService))
        .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
        .build()

      val selfEmploymentList = aSequenceTadesJourneyStatusesModel.map(TradesJourneyStatuses.toViewModel(_, taxYear)(messages(application)))

      running(application) {
        when(mockService.getCompletedTradeDetails(any, meq(currTaxYear), any)(any)) thenReturn apiResultT(aSequenceTadesJourneyStatusesModel)
        when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn Future.successful(Right(Some(true)))

        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(currTaxYear, aNoddyUser, JourneyStatus.Completed, selfEmploymentList)(
          request,
          messages(application)).toString
      }
    }

    "must return OK and display no Self-employments" - {
      "when an empty sequence of employments is returned from the backend" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {
          when(mockService.getCompletedTradeDetails(any, meq(currTaxYear), any)(any)) thenReturn apiResultT(Nil)
          when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn Future(Right(Some(true)))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[TaskListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(currTaxYear, aNoddyUser, JourneyStatus.Completed, Nil)(request, messages(application)).toString
        }
      }
      "when the review of trade details has not been completed" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        val selfEmploymentList = Seq.empty

        running(application) {
          mockConnector.getJourneyState(*[String], *[String], taxYear, *[String])(*, *) returns Future.successful(Right(Some(false)))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[TaskListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(currTaxYear, aNoddyUser, JourneyStatus.InProgress, selfEmploymentList)(
            request,
            messages(application)).toString
        }
      }
      "when the review of trade details has not been started" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        val selfEmploymentList = Seq.empty

        running(application) {
          mockConnector.getJourneyState(*[String], *[String], taxYear, *[String])(*, *) returns Future.successful(Right(None))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[TaskListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(currTaxYear, aNoddyUser, JourneyStatus.CheckOurRecords, selfEmploymentList)(
            request,
            messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery when an error response is returned" - {
      "from the service" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {
          when(mockService.getCompletedTradeDetails(any, meq(currTaxYear), any)(any)) thenReturn
            leftApiResultT(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error")))
          when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn Future(Right(Some(true)))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "from the connector" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {
          when(mockService.getCompletedTradeDetails(any, meq(currTaxYear), any)(any)) thenReturn apiResultT(aSequenceTadesJourneyStatusesModel)
          when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn
            Future(Left(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(currTaxYear).url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
