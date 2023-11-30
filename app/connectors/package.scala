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

import cats.implicits.catsSyntaxEitherId
import connectors.httpParser.JourneyStateParser.pagerDutyError
import models.common.Mtditid
import models.errors.HttpError
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

package object connectors {
  type NoContentResponse = Either[HttpError, Unit]

  /** Helper method to add necessary headers when calling endpoints
    */
  def post[A: Writes](http: HttpClient, url: String, mtditid: Mtditid, body: A)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[HttpError, Unit]] =
    http.POST[A, Either[HttpError, Unit]](url, body)(
      wts = implicitly[Writes[A]],
      rds = NoContentHttpReads,
      hc = hc.withExtraHeaders(headers = "mtditid" -> mtditid.value),
      ec = ec
    )

  object NoContentHttpReads extends HttpReads[NoContentResponse] {

    override def read(method: String, url: String, response: HttpResponse): NoContentResponse =
      response.status match {
        case status if status >= 200 && status <= 299 => ().asRight
        case _                                        => pagerDutyError(response).asLeft
      }

  }
}
