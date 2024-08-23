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

import base.SpecBase._
import builders.TradesJourneyStatusesBuilder.{aSequenceTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel}
import builders.UserBuilder.aNoddyUser
import cats.implicits._
import controllers.TaskListControllerSpec._
import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.journeys.routes
import models.common.JourneyStatus
import models.common.TaxYear.dateNow
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody}
import models.common.Journey.TradeDetails
import models.journeys.{JourneyNameAndStatus, TaskList, TaskListWithRequest}
import models.requests.TradesJourneyStatuses
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.controllers.actions.StubSubmittedDataRetrievalActionProvider
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.journeys.taskList.NationalInsuranceContributionsViewModel
import views.html.journeys.TaskListView

class TaskListControllerSpec extends AnyWordSpec with MockitoSugar {
  val nino       = "AA370343B"
  val user: User = User(mtditid.value, None, nino, AffinityGroup.Individual.toString)

  private val stubActionProvider = StubSubmittedDataRetrievalActionProvider()
  private val stubService        = SelfEmploymentServiceStub()

  private def nationalInsuranceEmptySummary(messages: Messages): SummaryList =
    NationalInsuranceContributionsViewModel.buildSummaryList(
      None,
      List(anEmptyTadesJourneyStatusesModel),
      dateNow.minusYears(16),
      List.empty,
      taxYear)(messages)

  "onPageLoad" should {

    "must return OK and display Self-employments when review of trade details has been completed" in {
      val application = createApp(
        stubActionProvider.copy(
          loadTaskListRes =
            taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.Completed).some, aSequenceTadesJourneyStatusesModel).asRight
        ),
        stubService
      )

      val selfEmploymentList =
        aSequenceTadesJourneyStatusesModel.map(TradesJourneyStatuses.toViewModel(_, taxYear, Some(emptyUserAnswersAccrual))(messages(application)))

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(
        taxYear,
        fakeUser,
        JourneyStatus.Completed,
        selfEmploymentList,
        nationalInsuranceEmptySummary(messages(application)))(fakeOptionalRequest, messages(application)).toString
    }

    "must return OK and display no Self-employments when an empty sequence of employments is returned from the backend" in {
      val application = createApp(
        stubActionProvider.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.Completed).some, Nil).asRight
        ),
        stubService)

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.Completed, Nil, nationalInsuranceEmptySummary(messages(application)))(
        fakeOptionalRequest,
        messages(application)).toString
    }

    "must return OK and display no Self-employments when the review of trade details has not been completed" in {
      val application = createApp(
        stubActionProvider.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.InProgress).some, Nil).asRight
        ),
        stubService)

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        taxYear,
        aNoddyUser,
        JourneyStatus.InProgress,
        Nil,
        nationalInsuranceEmptySummary(messages(application)))(fakeOptionalRequest, messages(application)).toString
    }

    "must return OK and display no Self-employments when the review of trade details has not been started" in {
      val application = createApp(
        stubActionProvider.copy(
          loadTaskListRes = taskListRequest(JourneyNameAndStatus(TradeDetails, JourneyStatus.CheckOurRecords).some, Nil).asRight
        ),
        stubService
      )
      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        taxYear,
        aNoddyUser,
        JourneyStatus.CheckOurRecords,
        Nil,
        nationalInsuranceEmptySummary(messages(application)))(fakeOptionalRequest, messages(application)).toString
    }
  }

  "must redirect to Journey Recovery when an error response is returned" should {
    "from the service" in {
      val application = createApp(
        stubActionProvider.copy(
          loadTaskListRes =
            ConnectorResponseError("method", "url", HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))).asLeft
        ),
        stubService
      )

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}

object TaskListControllerSpec {
  def taskListRequest(tradeDetails: Option[JourneyNameAndStatus], businesses: List[TradesJourneyStatuses]): TaskListWithRequest =
    TaskListWithRequest(TaskList(tradeDetails, businesses, None), fakeOptionalRequest)
}
