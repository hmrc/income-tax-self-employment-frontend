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
import models.common.{BusinessId, Mtditid, Nino, TaxYear}
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.BAD_REQUEST
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures {

  protected val businessId: BusinessId = BusinessId("someBusinessId")
  protected val nino: Nino             = Nino("someNino")
  protected val mtditid: Mtditid       = IntegrationBaseSpec.mtditid
  protected val taxYear: TaxYear       = TaxYear(LocalDate.now().getYear)

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(sys.env.get("INTEGRATION_TEST_PATIENCE_TIMEOUT_SEC").fold(2)(x => Integer.parseInt(x)), Seconds),
    interval = Span(500, Millis)
  )

  protected val headersSentToBE: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", mtditid.value))

  protected def parsingError(method: String, url: String): ServiceError =
    ConnectorResponseError(method, url, HttpError(BAD_REQUEST, HttpErrorBody.parsingError))

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  protected lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  protected def buildClient(urlandUri: String): WSRequest =
    ws
      .url(s"http://localhost:$port$urlandUri")
      .withFollowRedirects(false)
}

object IntegrationBaseSpec {
  val mtditid: Mtditid = Mtditid("mtditid")
}
