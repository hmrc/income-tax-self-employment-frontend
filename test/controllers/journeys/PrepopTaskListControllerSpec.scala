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

import base.SpecBase._
import builders.TradesJourneyStatusesBuilder.aSequenceTadesJourneyStatusesModel
import builders.UserBuilder.aNoddyUser
import cats.implicits._
import controllers.TaskListControllerSpec._
import controllers.actions.AuthenticatedIdentifierAction.User
import models.common.JourneyStatus
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody}
import models.journeys.Journey.TradeDetails
import models.journeys.{JourneyNameAndStatus, TaskList, TaskListWithRequest}
import models.requests.TradesJourneyStatuses
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.controllers.actions.StubSubmittedDataRetrievalActionProvider
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.journeys.PrepopTaskListView

class PrepopTaskListControllerSpec extends AnyWordSpec with MockitoSugar {
  val nino       = "AA370343B"
  val user: User = User(mtditid.value, None, nino, AffinityGroup.Individual.toString)

  private val stubService = StubSubmittedDataRetrievalActionProvider()

  "onPageLoad" should {

    "must return OK and display Self-employments when review of trade details has been completed" in {
      val application = createApp(
        stubService.copy(
          loadTaskListRes =
            taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.Completed).some, aSequenceTadesJourneyStatusesModel).asRight
        ))

      val selfEmploymentList =
        aSequenceTadesJourneyStatusesModel.map(TradesJourneyStatuses.toPrepopViewModel(_, taxYear)(messages(application)))

      val request = FakeRequest(GET, routes.PrepopTaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[PrepopTaskListView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(taxYear, fakeUser, JourneyStatus.Completed, selfEmploymentList)(
        fakeOptionalRequest,
        messages(application)).toString
    }

    "must return OK and display no Self-employments when an empty sequence of employments is returned from the backend" in {
      val application = createApp(
        stubService.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.Completed).some, Nil).asRight
        ))

      val request = FakeRequest(GET, routes.PrepopTaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[PrepopTaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.Completed, Nil)(fakeOptionalRequest, messages(application)).toString
    }
    "must return OK and display no Self-employments when the review of trade details has not been completed" in {
      val application = createApp(
        stubService.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.InProgress).some, Nil).asRight
        ))

      val request = FakeRequest(GET, routes.PrepopTaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[PrepopTaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.InProgress, Nil)(fakeOptionalRequest, messages(application)).toString
    }
    "must return OK and display no Self-employments when the review of trade details has not been started" in {
      val application = createApp(
        stubService.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.CheckOurRecords).some, Nil).asRight
        ))
      val request = FakeRequest(GET, routes.PrepopTaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[PrepopTaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.CheckOurRecords, Nil)(
        fakeOptionalRequest,
        messages(application)).toString
    }
  }

  "must redirect to Journey Recovery when an error response is returned" should {
    "from the service" in {
      val application = createApp(
        stubService.copy(
          loadTaskListRes =
            ConnectorResponseError("method", "url", HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))).asLeft
        ))

      val request = FakeRequest(GET, routes.PrepopTaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}

object PrepopTaskListControllerSpec {
  def taskListRequest(tradeDetails: Option[JourneyNameAndStatus],
                      businesses: List[TradesJourneyStatuses],
                      nationalInsuranceContributions: List[JourneyNameAndStatus]): TaskListWithRequest =
    TaskListWithRequest(TaskList(tradeDetails, businesses, nationalInsuranceContributions), fakeOptionalRequest)
}
