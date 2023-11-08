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
import connectors.httpParser.JourneyStateParser.JourneyStateResponse
import helpers.WiremockSpec
import models.errors.{HttpError, HttpErrorBody}
import models.journeys.TradeDetails
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.Future

class JourneyStateConnectorISpec extends WiremockSpec {

  def appConfig(businessApiHost: String = "localhost"): FrontendAppConfig =
    new FrontendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val selfEmploymentBEBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
    }

  val internalHost = "localhost"
  val underTest = new SelfEmploymentConnector(httpClient, appConfig(internalHost))

  val mtditid: String = "mtditid"
  val journey = TradeDetails.toString
  val taxYear = LocalDate.now().getYear
  val businessId = "business-Id-01"

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  val headersSentToBE: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", mtditid))

  ".getJourneyState" should {
    val getJourneyStateUrl = s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear"
    val completeState = false

    behave like journeyStateRequestIsSuccessful(NO_CONTENT,
      Right(None),
      () => stubGetWithoutResponseBody(getJourneyStateUrl, NO_CONTENT),
      () => underTest.getJourneyState(businessId, journey, taxYear, mtditid)
    )

    behave like journeyStateRequestIsSuccessful(OK,
      Right(Some(completeState)),
      () => stubGetWithResponseBody(getJourneyStateUrl, OK, completeState.toString, headersSentToBE),
      () => underTest.getJourneyState(businessId, journey, taxYear, mtditid)
    )

    behave like journeyStateRequestReturnsError(
      () => stubGetWithResponseBody(getJourneyStateUrl, BAD_REQUEST,
        Json.obj("code" -> "PARSING_ERROR", "reason" -> "Error parsing response from CONNECTOR").toString(), headersSentToBE
      ),
      () => underTest.getJourneyState(businessId, journey, taxYear, mtditid)
    )
  }

  ".saveJourneyState" should {

    val completeState = true
    val saveJourneyStateUrl = s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear/${completeState.toString}"

    behave like journeyStateRequestIsSuccessful(NO_CONTENT,
      Right(None),
      () => stubPutWithoutResponseBody(saveJourneyStateUrl, NO_CONTENT),
      () => underTest.saveJourneyState(businessId, journey, taxYear, completeState, mtditid)
    )

    behave like journeyStateRequestReturnsError(
      () => stubPutWithResponseBody(saveJourneyStateUrl, BAD_REQUEST,
        Json.obj("code" -> "PARSING_ERROR", "reason" -> "Error parsing response from CONNECTOR").toString(),
        headersSentToBE
      ),
      () => underTest.saveJourneyState(businessId, journey, taxYear, completeState, mtditid)
    )
  }


  def journeyStateRequestIsSuccessful(expStatus: Int, expectedResult: JourneyStateResponse, stubs: () => Unit,
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
      result mustBe Left(HttpError(BAD_REQUEST, HttpErrorBody.parsingError))
    }

}
