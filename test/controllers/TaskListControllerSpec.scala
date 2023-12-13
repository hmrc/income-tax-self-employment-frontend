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
import builders.TradesJourneyStatusesBuilder.aSequenceTadesJourneyStatusesModel
import builders.UserBuilder.aNoddyUser
import common.{apiResultT, leftApiResultT}
import connectors.SelfEmploymentConnector
import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.journeys.routes
import models.common.{JourneyStatus, Mtditid, Nino, TaxYear}
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody}
import models.journeys.Journey.TradeDetails
import models.requests.TradesJourneyStatuses
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.auth.core.AffinityGroup
import views.html.journeys.TaskListView

class TaskListControllerSpec extends AnyWordSpec with MockitoSugar {

  val nino       = "AA370343B"
  val user: User = User(mtditid, None, nino, AffinityGroup.Individual.toString)

  val mockService: SelfEmploymentServiceBase = mock[SelfEmploymentServiceBase]
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
    .overrides(bind[SelfEmploymentServiceBase].toInstance(mockService))
    .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
    .build()
  val selfEmploymentList =
    aSequenceTadesJourneyStatusesModel.map(TradesJourneyStatuses.toViewModel(_, taxYear, Some(emptyUserAnswers))(messages(application)))

  when(mockService.getCompletedTradeDetails(anyNino, anyTaxYear, anyMtditid)(any)) thenReturn apiResultT(aSequenceTadesJourneyStatusesModel)
  when(mockService.getJourneyStatus(meq(TradeDetails), anyNino, anyTaxYear, anyMtditid)(any)) thenReturn apiResultT(JourneyStatus.Completed)

  "onPageLoad" should {
    "must return OK and display Self-employments when review of trade details has been completed" in {
      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.Completed, selfEmploymentList)(
        request,
        messages(application)).toString
    }

    "must return OK and display no Self-employments when an empty sequence of employments is returned from the backend" in {
      when(mockService.getCompletedTradeDetails(anyNino, anyTaxYear, anyMtditid)(any)) thenReturn apiResultT(Nil)

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.Completed, Nil)(request, messages(application)).toString
    }
    "must return OK and display no Self-employments when the review of trade details has not been completed" in {
      when(mockService.getJourneyStatus(meq(TradeDetails), Nino(any()), TaxYear(any()), Mtditid(any()))(any)) thenReturn apiResultT(
        JourneyStatus.InProgress)

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.InProgress, Nil)(request, messages(application)).toString
    }
    "must return OK and display no Self-employments when the review of trade details has not been started" in {
      when(mockService.getJourneyStatus(meq(TradeDetails), Nino(any()), TaxYear(any()), Mtditid(any()))(any)) thenReturn apiResultT(
        JourneyStatus.CheckOurRecords)
      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[TaskListView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(taxYear, aNoddyUser, JourneyStatus.CheckOurRecords, Nil)(request, messages(application)).toString
    }
  }

  "must redirect to Journey Recovery when an error response is returned" should {
    "from the service" in {
      when(mockService.getCompletedTradeDetails(anyNino, anyTaxYear, anyMtditid)(any)) thenReturn
        leftApiResultT(ConnectorResponseError(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))))
      when(mockService.getJourneyStatus(meq(TradeDetails), Nino(any()), TaxYear(any()), Mtditid(any()))(any)) thenReturn apiResultT(
        JourneyStatus.Completed)

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
    }
    "from the connector" in {
      when(mockService.getCompletedTradeDetails(anyNino, anyTaxYear, anyMtditid)(any)) thenReturn apiResultT(aSequenceTadesJourneyStatusesModel)
      when(mockService.getJourneyStatus(meq(TradeDetails), Nino(any()), TaxYear(any()), Mtditid(any()))(any)) thenReturn
        leftApiResultT(ConnectorResponseError(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))))

      val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
