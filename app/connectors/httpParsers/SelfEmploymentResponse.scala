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

package connectors.httpParsers

import models.errors.APIErrorBody.APIStatusError
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object SelfEmploymentResponse extends APIParser {
  type SelfEmploymentResponse = Either[APIStatusError, Unit]

  val parserName: String = "SelfEmploymentResponse"
  val service: String = "income-tax-self-employment"

  implicit object SelfEmploymentHttpReads extends HttpReads[SelfEmploymentResponse] {
    override def read(method: String, url: String, response: HttpResponse): SelfEmploymentResponse =
      response.status match {
        case NO_CONTENT => Right(())
        case _ => pagerDutyError(response)
      }
  }
  implicit object SelfEmploymentHttpWrites extends OWrites[String] {
    override def writes(o: String): JsObject = Json.obj()
  }
}
