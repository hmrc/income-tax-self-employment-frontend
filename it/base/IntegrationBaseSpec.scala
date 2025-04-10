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

package base

import com.github.tomakehurst.wiremock.http.HttpHeader
import common.SessionValues
import integrationData.TimeData
import models.common.{BusinessId, Mtditid, Nino, TaxYear}
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.BAD_REQUEST
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, SessionId}
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.DefaultSessionCookieCryptoFilter
import utils.TimeMachine

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures {

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  when(mockTimeMachine.now).thenReturn(TimeData.testDate)

  protected val businessId: BusinessId = BusinessId("someBusinessId")
  protected val nino: Nino             = Nino("AA123123A")
  protected val mtditid: Mtditid       = IntegrationBaseSpec.mtditid
  protected val taxYear: TaxYear       = TaxYear(mockTimeMachine.now.getYear)

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(sys.env.get("INTEGRATION_TEST_PATIENCE_TIMEOUT_SEC").fold(2)(x => Integer.parseInt(x)), Seconds),
    interval = Span(500, Millis)
  )

  protected val headersSentToBE: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", mtditid.value))

  protected def parsingError(method: String, url: String): ServiceError =
    ConnectorResponseError(method, url, HttpError(BAD_REQUEST, HttpErrorBody.parsingError))

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  val baseSessionValues = Map(
    SessionValues.CLIENT_MTDITID -> mtditid.value,
    SessionValues.CLIENT_NINO    -> nino.value
  )

  protected lazy val ws: WSClient = app.injector.instanceOf[WSClient]
  val sessionCookieFilter         = app.injector.instanceOf[DefaultSessionCookieCryptoFilter]
  sessionCookieFilter.sessionBaker.deserialize(baseSessionValues)

  protected def buildClient(urlandUri: String,
                            extraHeaders: Option[Map[String, String]] = None,
                            extraCookies: Option[Map[String, String]] = None): WSRequest = {
    val session = DefaultWSCookie(
      name = "PLAY_SESSION",
      value = "EEEEGG"
    )

    ws
      .url(s"http://localhost:$port$urlandUri")
      .withFollowRedirects(false)
      .addHttpHeaders(
        HeaderNames.authorisation -> "Bearer BXQ3/Treo4kQCZvVcCqKPhA7wE/2hNqCz4BjnFzEN5m6lmzrFrQI96Au2BZrW0e9WLpDsptzxUoUEaw0V1MQH6EXGq/8151X26j/qnvuZUXEsWcJ6ru7Fr+/ci2kcBf4NHKTPCIju1pIGJG5Oqihp7aDpRrleO+Ik/A5cDedlvf9KwIkeIPK/mMlBESjue4V")
      .withCookies(session)
  }
}

object IntegrationBaseSpec {
  val mtditid: Mtditid = Mtditid("555555555")
}
