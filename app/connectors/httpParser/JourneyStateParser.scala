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

import models.errors.ServiceError
import models.journeys.JourneyNameAndStatus
import play.api.http.Status.{CREATED, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object JourneyStateParser extends HttpParser {
  type JourneyStateResponse = Either[ServiceError, JourneyNameAndStatus]
  type JourneyStateResponse2 = Either[ServiceError, Option[Boolean]]

  val parserName: String = "JourneyStateParser"
  val service: String    = "income-tax-self-employment"

  implicit object JourneyStateHttpReads extends HttpReads[JourneyStateResponse2] {
    override def read(method: String, url: String, response: HttpResponse): JourneyStateResponse2 =
      response.status match {
        case OK         => Right(Some(response.body.toBoolean))
        case CREATED    => Right(None)
        case NO_CONTENT => Right(None)
        case _          => Left(pagerDutyError(response))
      }
  }

  implicit object JourneyStateHttpWrites extends OWrites[String] {
    override def writes(o: String): JsObject = Json.obj()
  }
}
