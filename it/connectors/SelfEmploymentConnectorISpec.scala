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

import builders.BusinessDataBuilder.aGetBusinessDataRequestStr
import com.github.tomakehurst.wiremock.http.HttpHeader
import config.FrontendAppConfig
import helpers.WiremockSpec
import models.errors.APIErrorBody.{APIError, APIStatusError}
import models.requests.GetBusiness
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class SelfEmploymentConnectorISpec extends WiremockSpec {

  lazy val connector: SelfEmploymentConnector = app.injector.instanceOf[SelfEmploymentConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(businessApiHost: String): FrontendAppConfig = new FrontendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val selfEmploymentBEBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
  }

  val (nino, mtdId) = ("123456789", "1234567890123456")

  val url = s"income-tax-self-employment/business/$nino"

  val headersSentToBE: Seq[HttpHeader] = Seq(
    new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue"),
    new HttpHeader("mtditid", mtdId)
  )


  ".getBusinesses" should {

    "return 200" when {
      val internalHost = "localhost"

      val expectedResponseBody = aGetBusinessDataRequestStr

      val expectedResult = Json.parse(expectedResponseBody).as[GetBusiness]
      stubGetWithResponseBody(url, OK, expectedResponseBody, headersSentToBE)

      implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
      val result = await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusinesses(nino)(hc, ec))
      result mustBe Right(expectedResult)
    }

    Seq(NOT_FOUND, BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { errorStatus =>
      val (invalidIdType, invalidReason) = {
        val invalidParam = "Submission has not passed validation. Invalid parameter"
        ("INVALID_NINO", s"$invalidParam NINO")
      }

      val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason, "errorType" -> "DOWNSTREAM_ERROR_CODE")

      s"return a $errorStatus" in {
        stubGetWithResponseBody(url, errorStatus, errorResponseBody.toString(), headersSentToBE)
        auditStubs()

        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val result = await(connector.getBusinesses(nino)(hc, ec))
        result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
      }
    }

    s"return a parsing error when the HeaderCarrier is insufficient" in {
      val (invalidIdType, invalidReason) = ("PARSING_ERROR", "Error parsing response from API")
      val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason, "errorType" -> "DOWNSTREAM_ERROR_CODE")
      val errorStatus = 404

      stubGetWithResponseBody(url, errorStatus, errorResponseBody.toString(), headersSentToBE)
      auditStubs()

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getBusinesses(nino)(hc, ec))
      result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
    }
  }
  ".getBusiness" should {

    val businessId = "ABC123"

    "return 200" when {
      val internalHost = "localhost"

      val expectedResponseBody = aGetBusinessDataRequestStr

      val expectedResult = Json.parse(expectedResponseBody).as[GetBusiness]
      stubGetWithResponseBody(url, OK, expectedResponseBody, headersSentToBE)

      implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
      val result = await(new SelfEmploymentConnector(httpClient, appConfig(internalHost)).getBusiness(nino, businessId)(hc, ec))
      result mustBe Right(expectedResult)
    }

    Seq(NOT_FOUND, BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { errorStatus =>
      val (invalidIdType, invalidReason) = {
        val invalidParam = "Submission has not passed validation. Invalid parameter"
        ("INVALID_NINO", s"$invalidParam NINO")
      }

      val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason, "errorType" -> "DOWNSTREAM_ERROR_CODE")

      s"return a $errorStatus" in {
        stubGetWithResponseBody(url, errorStatus, errorResponseBody.toString(), headersSentToBE)
        auditStubs()

        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val result = await(connector.getBusiness(nino, businessId)(hc, ec))
        result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
      }
    }

    s"return a parsing error when the HeaderCarrier is insufficient" in {
      val (invalidIdType, invalidReason) = ("PARSING_ERROR", "Error parsing response from API")
      val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason, "errorType" -> "DOWNSTREAM_ERROR_CODE")
      val errorStatus = 404

      stubGetWithResponseBody(url, errorStatus, errorResponseBody.toString(), headersSentToBE)
      auditStubs()

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getBusiness(nino, businessId)(hc, ec))
      result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
    }
  }
}
