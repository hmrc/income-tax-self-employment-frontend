/*
 * Copyright 2025 HM Revenue & Customs
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

import base.IntegrationBaseSpec
import helpers.WiremockSpec
import models.errors.HttpErrorBody.SingleErrorBody
import models.errors.ServiceError.{CannotReadJsonError, ConnectorResponseError}
import models.errors.{HttpError, ServiceError}
import models.session.SessionData
import play.api.http.Status._
import play.api.libs.json.{Json, __}
import uk.gov.hmrc.http.SessionKeys.sessionId

class SessionDataConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  lazy val connector: SessionDataConnector = app.injector.instanceOf[SessionDataConnector]

  val stubGetUrl                       = s"/income-tax-session-data"
  val sessionDataResponse: SessionData = SessionData(mtditid = mtditid.value, nino = nino.value, sessionId = sessionId)

  type SessionDataResponse = Either[ServiceError, Option[SessionData]]

  "calling .getSessionData()" should {

    "return session data" when {

      "downstream response is successful and includes valid JSON" in {
        stubGetWithResponseBody(stubGetUrl, OK, Json.toJson(sessionDataResponse).toString())

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result mustBe Right(Some(sessionDataResponse))
      }
    }

    "return None" when {

      "downstream returns NOT_FOUND" in {
        stubGetWithoutResponseBody(stubGetUrl, NOT_FOUND)

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result mustBe Right(None)
      }

      "downstream returns NO_CONTENT" in {
        stubGetWithoutResponseBody(stubGetUrl, NO_CONTENT)

        val result: SessionDataResponse = connector.getSessionData(hc).futureValue
        result mustBe Right(None)
      }
    }

    "return Left(error)" when {

      "downstream returns OK but with an invalid response payload" in {
        stubGetWithResponseBody(stubGetUrl, OK, "{}")

        connector.getSessionData(hc).futureValue match {
          case Left(value: CannotReadJsonError) =>
            value.details.map(_._1) mustBe Seq(
              __ \ "sessionId",
              __ \ "nino",
              __ \ "mtditid"
            )
          case Left(_)  => fail("Expected a CannotReadJsonError, but got a different error")
          case Right(_) => fail("Expected a Left error response, but got a Right")
        }
      }

      "downstream fails with Internal Error" in {

        val apiError = SingleErrorBody("INTERNAL_SERVER_ERROR", "Internal server error")
        stubGetWithResponseBody(stubGetUrl, INTERNAL_SERVER_ERROR, Json.toJson(apiError).toString())

        connector.getSessionData(hc).futureValue match {
          case Left(value) =>
            value mustBe ConnectorResponseError(
              method = "GET",
              url = s"http://localhost:$wireMockPort/income-tax-session-data",
              originalHttpError = HttpError(INTERNAL_SERVER_ERROR, apiError)
            )
          case Left(_)  => fail("Expected a ConnectorResponseError, but got a different error")
          case Right(_) => fail("Expected a Left error response, but got a Right")
        }
      }

      "downstream returns SERVICE_UNAVAILABLE" in {

        val apiError = SingleErrorBody("SERVICE_UNAVAILABLE", "Service unavailable error")
        stubGetWithResponseBody(stubGetUrl, SERVICE_UNAVAILABLE, Json.toJson(apiError).toString())

        connector.getSessionData(hc).futureValue match {
          case Left(value) =>
            value mustBe ConnectorResponseError(
              method = "GET",
              url = s"http://localhost:$wireMockPort/income-tax-session-data",
              originalHttpError = HttpError(SERVICE_UNAVAILABLE, apiError)
            )
          case Left(_)  => fail("Expected a ConnectorResponseError, but got a different error")
          case Right(_) => fail("Expected a Left error response, but got a Right")
        }
      }

      "downstream fails with unknown error" in {

        val apiError = SingleErrorBody("TOO_MANY_REQUESTS", "Service unavailable error")
        stubGetWithResponseBody(stubGetUrl, TOO_MANY_REQUESTS, Json.toJson(apiError).toString())

        connector.getSessionData(hc).futureValue match {
          case Left(value) =>
            value mustBe ConnectorResponseError(
              method = "GET",
              url = s"http://localhost:$wireMockPort/income-tax-session-data",
              originalHttpError = HttpError(INTERNAL_SERVER_ERROR, apiError)
            )
          case Left(_)  => fail("Expected a ConnectorResponseError, but got a different error")
          case Right(_) => fail("Expected a Left error response, but got a Right")
        }
      }
    }
  }
}
