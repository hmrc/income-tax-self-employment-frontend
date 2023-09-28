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
import connectors.httpParser.GetBusinessesHttpParser.GetBusinessesResponse
import connectors.httpParser.JourneyStateParser.JourneyStateResponse
import helpers.WiremockSpec
import models.TradeDetails
import models.errors.HttpErrorBody.SingleErrorBody
import models.errors.{HttpError, HttpErrorBody}
import models.mdtp.BusinessData
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.Future

class SelfEmploymentConnectorISpec extends WiremockSpec {
  
  def appConfig(businessApiHost: String = "localhost"): FrontendAppConfig =
    new FrontendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val selfEmploymentBEBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
    }

  val internalHost = "localhost"
  val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

  val nino = "AA370343B"
  val mtditid: String = "mtditid"
  val tradeDetailsJourney = TradeDetails.toString
  val taxYear = LocalDate.now().getYear
  val businessId = tradeDetailsJourney + "-" + nino

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  val headersSentToBE: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  ".saveJourneyState" should {

    val saveJourneyState = s"/income-tax-self-employment/completed-section/$businessId/$tradeDetailsJourney/$taxYear/true"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

    behave like journeyStateRequestReturnsNoContent(
      () => stubPutWithoutResponseBody(saveJourneyState, NO_CONTENT))(
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).saveJourneyState(
        businessId, tradeDetailsJourney, taxYear, true, mtditid)(hc, ec)))
  }

  ".saveJourneyState" should {

    val saveJourneyState = s"/income-tax-self-employment/completed-section/$businessId/$tradeDetailsJourney/$taxYear/true"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))


    behave like journeyStateRequestReturnsError(
      () => stubPutWithResponseBody(saveJourneyState,
        BAD_REQUEST,
        Json.obj("code" -> "PARSING_ERROR", "reason" -> "Error parsing response from CONNECTOR").toString(),
        headersSentToBE))(
      () => new SelfEmploymentConnector(httpClient, appConfig(internalHost)).saveJourneyState(
        businessId, tradeDetailsJourney, taxYear, true, mtditid)(hc, ec))
  }

  def saveJourneyStateRequestReturnsNoContent(getUrl: String, block: () => JourneyStateResponse): Unit =
    "return a 204 response and a JourneyStateResponse model" in {
      stubPutWithoutResponseBody(getUrl, NO_CONTENT)
      val result = block()
      result mustBe Right(None)
    }

  def journeyStateRequestReturnsNoContent(stubs: () => Unit)(block: () => JourneyStateResponse): Unit =
    "return a 204 response and a SelfEmploymentResponse model" in {
      stubs()
      val result = block()
      result mustBe Right(None)
    }

  def journeyStateRequestReturnsError(stubs: () => Unit)(block: () => Future[JourneyStateResponse]): Unit =
    "return an error when the connector returns an error" in {
      stubs()
      val result = await(block())
      result mustBe Left(HttpError(BAD_REQUEST, HttpErrorBody.parsingError))
      result mustBe Left(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR")))
    }
  

  ".getBusiness" should {
    val businessId = "ABC123"
    val getBusiness = s"/income-tax-self-employment/individuals/business/details/$nino/$businessId"
    
    behave like businessRequestReturnsOk(getBusiness, () => underTest.getBusiness(nino, businessId, mtditid))
    behave like businessRequestReturnsError(getBusiness, () => underTest.getBusiness(nino, businessId, mtditid))
  }

  ".getBusinesses" should {
    val getBusinesses = s"/income-tax-self-employment/individuals/business/details/$nino/list"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
    val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

    behave like businessRequestReturnsOk(getBusinesses, () => underTest.getBusinesses(nino, mtditid))
    behave like businessRequestReturnsError(getBusinesses, () => underTest.getBusinesses(nino, mtditid))
  }

  def businessRequestReturnsOk(getUrl: String, block: () => Future[GetBusinessesResponse]): Unit = {
    "return a 200 response and a GetBusinessRequest model" in {
      val expectedResponseBody = aGetBusinessDataRequestStr
      val expectedResult = Json.parse(expectedResponseBody).as[Seq[BusinessData]]
      stubGetWithResponseBody(getUrl, OK, expectedResponseBody, headersSentToBE)
      val result = await(block())
      result mustBe Right(expectedResult)
    }
    "return a 200 response but produce a json non-validation error" in {
      val expectedResponseBody = Json.obj("nonValidatingJson" -> "").toString()
      stubGetWithResponseBody(getUrl, OK, expectedResponseBody, headersSentToBE)
      val result = await(block())
      result mustBe Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError))
    }
  }

  def businessRequestReturnsError(getUrl: String, block: () => Future[GetBusinessesResponse]): Unit =
    for ((errorStatus, code, reason) <- Seq(
      (NOT_FOUND, "NOT_FOUND", "The remote endpoint has indicated that no data can be found."),
      (BAD_REQUEST, "INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO."),
      (INTERNAL_SERVER_ERROR, "SERVER_ERROR", "IF is currently experiencing problems that require live service intervention."),
      (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))) {

      s"return a $errorStatus error when the connector returns an error" in {
        stubGetWithResponseBody(getUrl, errorStatus,
          Json.obj("code" -> code, "reason" -> reason).toString(), headersSentToBE)
        val result = await(block())
        result mustBe Left(HttpError(errorStatus, SingleErrorBody(code, reason)))
      }
    }
    
  lazy val aGetBusinessDataRequestStr: String =
  """
      |[
      |{
      |   "businessId":"SJPR05893938418",
      |   "typeOfBusiness":"self-employment",
      |   "tradingName":"string",
      |   "yearOfMigration":"2022",
      |   "accountingPeriods":[
      |      {
      |         "start":"2023-02-29",
      |         "end":"2024-02-29"
      |      }
      |   ],
      |   "firstAccountingPeriodStartDate":"2019-09-30",
      |   "firstAccountingPeriodEndDate":"2020-02-29",
      |   "latencyDetails":{
      |      "latencyEndDate":"2020-02-27",
      |      "taxYear1":"2019",
      |      "latencyIndicator1":"A",
      |      "taxYear2":"2020",
      |      "latencyIndicator2":"A"
      |   },
      |   "accountingType":"ACCRUAL",
      |   "commencementDate":"2023-04-06",
      |   "cessationDate":"2024-04-05",
      |   "businessAddressLineOne":"string",
      |   "businessAddressLineTwo  ":"string",
      |   "businessAddressLineThree":"string",
      |   "businessAddressLineFour ":"string",
      |   "businessAddressPostcode ":"string",
      |   "businessAddressCountryCode":"GB"
      |}
      |]
      |""".stripMargin

}
