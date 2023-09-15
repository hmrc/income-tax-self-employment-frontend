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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.FrontendAppConfig
import connectors.builders.BusinessDataBuilder.aGetBusinessDataRequestStr
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import connectors.httpParsers.SelfEmploymentResponse.SelfEmploymentResponse
import helpers.WiremockSpec
import models.DetailsCompletedSection.Yes
import models.errors.APIErrorBody.{APIError, APIStatusError}
import models.requests.GetBusinesses
import models.{APIErrorBodyModel, APIErrorModel, Journeys}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.Future

class SelfEmploymentConnectorISpec extends WiremockSpec {

  lazy val connector: SelfEmploymentConnector = app.injector.instanceOf[SelfEmploymentConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(businessApiHost: String = "localhost"): FrontendAppConfig = new FrontendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val selfEmploymentBEBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
  }

  val internalHost = "localhost"
  val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

  val nino = "123456789"
  val taxYear = LocalDate.now().getYear

  val headersSentToBE: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  ".saveJourneyState" should {

    val (journeyId, isComplete) = (Journeys.Income, Yes)
    val saveJourneyState = s"/completed-section/$nino/$journeyId/${isComplete.equals(Yes).toString}"
    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

    behave like journeyStateRequestReturnsNoContent(
      () => stubPutWithoutResponseBody(saveJourneyState, NO_CONTENT))(
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).saveJourneyState(nino, journeyId, taxYear, isComplete.equals(Yes))(hc)))


    behave like journeyStateRequestReturnsError(
      () => stubPutWithResponseBody(saveJourneyState,
        BAD_REQUEST,
        Json.obj("code" -> "INVALID_NINO", "reason" -> "Submission has not passed validation. Invalid parameter").toString(),
        headersSentToBE))(
      () => underTest.saveJourneyState(nino, journeyId, taxYear, isComplete.equals(Yes)))
  }

  ".getBusiness" should {

    val businessId = "ABC123"
    val getBusiness = s"/income-tax-self-employment/business/$nino/$businessId"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))


    behave like businessRequestReturnsOk(
      () => stubGetWithResponseBody(getBusiness, OK, aGetBusinessDataRequestStr, headersSentToBE))(
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusiness(nino, businessId)(hc)))


    behave like businessRequestReturnsError(
      () => stubGetWithResponseBody(getBusiness,
        BAD_REQUEST,
        Json.obj("code" -> "INVALID_NINO", "reason" -> "Submission has not passed validation. Invalid parameter", "errorType" -> "DOWNSTREAM_ERROR_CODE").toString(),
        headersSentToBE))(
      () => underTest.getBusiness(nino, businessId))
  }

  ".getBusinesses" should {

    val getBusinesses = s"/income-tax-self-employment/business/$nino"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
    val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

    behave like businessRequestReturnsOk(
      () => stubGetWithResponseBody(getBusinesses, OK, aGetBusinessDataRequestStr, headersSentToBE))(
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusinesses(nino)(hc)))

    behave like businessRequestReturnsError(
      () => stubGetWithResponseBody(getBusinesses,
        BAD_REQUEST,
        Json.obj("code" -> "INVALID_NINO", "reason" -> "Submission has not passed validation. Invalid parameter", "errorType" -> "DOWNSTREAM_ERROR_CODE").toString(),
        headersSentToBE))(
      () => underTest.getBusinesses(nino))
  }

  def journeyStateRequestReturnsNoContent(stubs: () => Unit)(block: () => SelfEmploymentResponse): Unit =
    "return a 204 response and a SelfEmploymentResponse model" in {
      stubs()
      val result = block()
      result mustBe Right(())
    }

  def journeyStateRequestReturnsError(stubs: () => Unit)(block: () => Future[SelfEmploymentResponse]): Unit =
    "return an error when the connector returns an error" in {
      stubs()
      val result = await(block())
      result mustBe Left(APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_NINO", "Submission has not passed validation. Invalid parameter")))
    }

  def businessRequestReturnsOk(stubs: () => Unit)(block: () => GetBusinessesResponse): Unit =
    "return a 200 response and a GetBusinessRequest model" in {
      val expectedResponseBody = aGetBusinessDataRequestStr
      val expectedResult = Json.parse(expectedResponseBody).as[GetBusinesses]
      stubs()
      val result = block()
      result mustBe Right(expectedResult)
    }

  def businessRequestReturnsError(stubs: () => Unit)(block: () => Future[GetBusinessesResponse]): Unit =
    "return an error when the connector returns an error" in {
      stubs()
      val result = await(block())
      result mustBe Left(APIStatusError(BAD_REQUEST, APIError("INVALID_NINO", "Submission has not passed validation. Invalid parameter")))
    }
}
