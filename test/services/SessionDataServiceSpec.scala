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

package services

import base.SpecBase
import config.MockAppConfig
import connectors.MockSessionDataConnector
import models.errors.ServiceError
import models.errors.ServiceError.{CannotParseJsonError, MissingAgentClientDetails}
import models.session.SessionData
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SessionDataServiceSpec extends SpecBase with MockAppConfig with MockSessionDataConnector {

  val testService: SessionDataService = new SessionDataService(
    sessionDataConnector = mockSessionDataConnector,
    config = mockAppConfig
  )

  val dummyError: ServiceError = CannotParseJsonError(new Exception("Dummy error"))

  ".getSessionData()" - {

    "when V&C Session Data service feature is enabled" - {

      "when the call to retrieve session data fails" - {

        "when the retrieval of fallback data from the session cookie also fails to find the clients details" - {

          "should return an error when fallback returns no data" in {
            MockAppConfig.sessionCookieServiceEnabled(true)
            MockSessionDataConnector.getSessionData(Left(dummyError))

            implicit val request: Request[_] = FakeRequest()

            val result: MissingAgentClientDetails = intercept[MissingAgentClientDetails](await(testService.getSessionData(sessionId)(request, hc)))
            result.message mustBe "Session Data service and Session Cookie both returned empty data"
          }
        }

        "when the fallback is successful and retrieves client MTDITID and NINO from the Session Cookie" - {

          "should return session data" in {
            MockAppConfig.sessionCookieServiceEnabled(true)
            MockSessionDataConnector.getSessionData(Left(dummyError))

            implicit val request: Request[_] = FakeRequest()
              .withSession(
                ("ClientNino", "AA111111A"),
                ("ClientMTDID", "12345678")
              )

            val result: SessionData = await(testService.getSessionData(sessionId)(request, hc))
            result mustBe SessionData(sessionId = sessionId, mtditid = "12345678", nino = "AA111111A")
          }
        }
      }

      "the call to retrieve session data from the downstream V&C service is successful" - {

        "return the session data" in {
          MockAppConfig.sessionCookieServiceEnabled(true)
          MockSessionDataConnector.getSessionData(Right(Some(sessionData)))

          implicit val request: Request[_] = FakeRequest()

          val result: SessionData = await(testService.getSessionData(sessionId)(request, hc))
          result mustBe sessionData
        }
      }
    }

    "V&C Session Data service feature is DISABLED" - {

      "the retrieval of fallback data from the session cookie also fails to find the clients details" - {

        "return an error when fallback returns no data" in {
          MockAppConfig.sessionCookieServiceEnabled(false)

          implicit val request: Request[_] = FakeRequest()

          val result: MissingAgentClientDetails = intercept[MissingAgentClientDetails](await(testService.getSessionData(sessionId)(request, hc)))
          result.message mustBe "Session Data service and Session Cookie both returned empty data"
        }
      }

      "the fallback is successful and retrieves client MTDITID and NINO from the Session Cookie" - {

        "return session data" in {
          MockAppConfig.sessionCookieServiceEnabled(false)

          implicit val request: Request[_] = FakeRequest()
            .withSession(
              ("ClientNino", "AA111111A"),
              ("ClientMTDID", "12345678")
            )

          val result: SessionData = await(testService.getSessionData(sessionId)(request, hc))
          result mustBe SessionData(sessionId = sessionId, mtditid = "12345678", nino = "AA111111A")
        }
      }
    }
  }
}
