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

package connectors.httpParser

import models.errors.HttpError
import models.requests.TradesJourneyStatuses
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetTradesStatusHttpParser extends HttpParser {
  type GetTradesStatusResponse = Either[HttpError, Seq[TradesJourneyStatuses]]

  override val parserName: String = "GetTradesStatusHttpParser"

  implicit object GetTradesStatusHttpReads extends HttpReads[GetTradesStatusResponse] {

    override def read(method: String, url: String, response: HttpResponse): GetTradesStatusResponse =
      response.status match {
        case OK => response.json.validate[Seq[TradesJourneyStatuses]].fold[GetTradesStatusResponse](
          _ => nonModelValidatingJsonFromAPI,
          parsedModel => Right(parsedModel)
        )
        case _ => pagerDutyError(response)
      }
  }
}
