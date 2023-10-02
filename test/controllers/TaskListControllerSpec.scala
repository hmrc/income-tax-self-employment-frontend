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
import builders.UserBuilder.aNoddyUser
import connectors.builders.TradesJourneyStatusesBuilder.aSequenceTaggedTradeDetailsModel
import controllers.actions.AuthenticatedIdentifierAction.User
import models.errors.{HttpError, HttpErrorBody}
import models.requests.TradesJourneyStatuses
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.TaskListView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class TaskListControllerSpec extends SpecBase with MockitoSugar {

  val taxYear = LocalDate.now().getYear
  val nino = "AA370343B"
  val mtditid = "mtditid"
  val user = User(mtditid, None, nino, AffinityGroup.Individual.toString)

  implicit val ec: ExecutionContext = ExecutionContext.global

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  "TaskListController .onPageLoad" - {

    "must return OK and the correct view" - {

      "with Self-employments when a sequence of completed trade details are returned from the backend" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService)).build()

        val selfEmploymentList = aSequenceTaggedTradeDetailsModel.map(TradesJourneyStatuses.toViewModel(_, taxYear)(messages(application)))

        running(application) {
          when(mockService.getCompletedTradeDetails(any, meq(taxYear), any)(any)) thenReturn Future(Right(aSequenceTaggedTradeDetailsModel))

          val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[TaskListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(taxYear, aNoddyUser, selfEmploymentList)(request, messages(application)).toString
        }
      }

      "with no Self-employments" - {
        "when an empty sequence is returned from the backend" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SelfEmploymentService].toInstance(mockService)).build()

          val selfEmploymentList = Seq.empty

          running(application) {
            when(mockService.getCompletedTradeDetails(any, meq(taxYear), any)(any)) thenReturn Future(Right(selfEmploymentList))

            val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[TaskListView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(taxYear, aNoddyUser, selfEmploymentList)(request, messages(application)).toString
          }
        }

        "when a Left(error) is returned from downstream" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SelfEmploymentService].toInstance(mockService)).build()

          val selfEmploymentList = Seq.empty
          val hTTPError = HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)

          running(application) {
            when(mockService.getCompletedTradeDetails(any, meq(taxYear), any)(any)) thenReturn Future(Left(hTTPError))

            val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[TaskListView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(taxYear, aNoddyUser, selfEmploymentList)(request, messages(application)).toString
          }
        }
      }

    }
  }
}
