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

import base.IntegrationBaseSpec
import connectors.httpParser.JourneyStateParser.JourneyStateResponse
import helpers.WiremockSpec
import models.journeys.Journey.TradeDetails
import play.api.http.Status._
import play.api.libs.json.Json

import scala.concurrent.Future

class JourneyStateConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private val journey = TradeDetails.toString

  private val connector = new SelfEmploymentConnector(httpClient, appConfig)

  ".getJourneyState" should {
    val getJourneyStateUrl = s"/income-tax-self-employment/completed-section/${businessId.value}/$journey/${taxYear.value}"
    val completeState      = false

    behave like journeyStateRequestIsSuccessful(
      NO_CONTENT,
      Right(None),
      () => stubGetWithoutResponseBody(getJourneyStateUrl, NO_CONTENT),
      () => connector.getJourneyState(businessId, journey, taxYear, mtditid)
    )

    behave like journeyStateRequestIsSuccessful(
      OK,
      Right(Some(completeState)),
      () => stubGetWithResponseBody(getJourneyStateUrl, OK, completeState.toString, headersSentToBE),
      () => connector.getJourneyState(businessId, journey, taxYear, mtditid)
    )

    behave like journeyStateRequestReturnsError(
      () =>
        stubGetWithResponseBody(
          getJourneyStateUrl,
          BAD_REQUEST,
          Json.obj("code" -> "PARSING_ERROR", "reason" -> "Error parsing response from CONNECTOR").toString(),
          headersSentToBE),
      () => connector.getJourneyState(businessId, journey, taxYear, mtditid)
    )
  }

  ".saveJourneyState" should {

    val completeState       = true
    val saveJourneyStateUrl = s"/income-tax-self-employment/completed-section/${businessId.value}/$journey/${taxYear.value}/${completeState.toString}"

    behave like journeyStateRequestIsSuccessful(
      NO_CONTENT,
      Right(None),
      () => stubPutWithoutResponseBody(saveJourneyStateUrl, NO_CONTENT),
      () => connector.saveJourneyState(businessId, journey, taxYear, completeState, mtditid)
    )

    behave like journeyStateRequestReturnsError(
      () =>
        stubPutWithResponseBody(
          saveJourneyStateUrl,
          BAD_REQUEST,
          Json.obj("code" -> "PARSING_ERROR", "reason" -> "Error parsing response from CONNECTOR").toString(),
          headersSentToBE),
      () => connector.saveJourneyState(businessId, journey, taxYear, completeState, mtditid)
    )
  }

  def journeyStateRequestIsSuccessful(expStatus: Int,
                                      expectedResult: JourneyStateResponse,
                                      stubs: () => Unit,
                                      block: () => Future[JourneyStateResponse]): Unit =
    s"return a $expStatus response and a SelfEmploymentResponse model" in {
      stubs()
      val result = await(block())
      result mustBe expectedResult
    }

  def journeyStateRequestReturnsError(stubs: () => Unit, block: () => Future[JourneyStateResponse]): Unit =
    "return an error when the connector returns an error" in {
      stubs()
      val result = await(block())
      result mustBe Left(httpError)
    }

}
