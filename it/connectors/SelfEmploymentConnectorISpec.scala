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
import connectors.httpParser.GetBusinessesHttpParser.GetBusinessesResponse
import helpers.WiremockSpec
import models.errors.HttpErrorBody.SingleErrorBody
import models.errors.{HttpError, HttpErrorBody}
import models.requests.GetBusinesses
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future

class SelfEmploymentConnectorISpec extends WiremockSpec {

  lazy val connector: SelfEmploymentConnector = app.injector.instanceOf[SelfEmploymentConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(businessApiHost: String = "localhost"): FrontendAppConfig =
    new FrontendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val selfEmploymentBEBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
    }

  val internalHost = "localhost"
  val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

  val (nino, mtdId) = ("123456789", "1234567890123456")

  val getBusinesses = s"/income-tax-self-employment/business/$nino"

  val headersSentToBE: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  ".getBusiness" should {
    val businessId = "ABC123"
    val getBusiness = s"/income-tax-self-employment/business/$nino/$businessId"
    
    behave like businessRequestReturnsOk(getBusiness,
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusiness(nino, mtdId, businessId))
    )
    behave like businessRequestReturnsError(getBusiness, () => underTest.getBusiness(nino, mtdId, businessId))
  }

  ".getBusinesses" should {

    val getBusinesses = s"/income-tax-self-employment/business/$nino"

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
    val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

    behave like businessRequestReturnsOk(getBusinesses,
      () => await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusinesses(nino, mtdId))
    )
    behave like businessRequestReturnsError(getBusinesses, () => underTest.getBusinesses(nino, mtdId))
  }

  def businessRequestReturnsOk(getUrl: String, block: () => GetBusinessesResponse): Unit = {
    "return a 200 response and a GetBusinessRequest model" in {
      val expectedResponseBody = aGetBusinessDataRequestStr
      val expectedResult = Json.parse(expectedResponseBody).as[GetBusinesses]
      stubGetWithResponseBody(getUrl, OK, expectedResponseBody, headersSentToBE)
      val result = block()
      result mustBe Right(expectedResult)
    }
    "return a 200 response but produce a json non-validation error" in {
      val expectedResponseBody = Json.obj("nonValidatingJson" -> "").toString()
      stubGetWithResponseBody(getUrl, OK, expectedResponseBody, headersSentToBE)
      val result = block()
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
}
