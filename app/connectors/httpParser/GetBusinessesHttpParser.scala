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

import models.domain.BusinessData
import models.errors.ServiceError
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetBusinessesHttpParser extends HttpParser {
  type GetBusinessesResponse = Either[ServiceError, Seq[BusinessData]]

  override val parserName: String = "GetBusinessHttpParser"

  implicit object GetBusinessesHttpReads extends HttpReads[GetBusinessesResponse] {

    override def read(method: String, url: String, response: HttpResponse): GetBusinessesResponse =
      response.status match {
        case OK =>
          response.json
            .validate[Seq[BusinessData]]
            .fold[GetBusinessesResponse](_ => Left(nonModelValidatingJsonFromAPI), parsedModel => Right(parsedModel))

        case _ => Left(unsafePagerDutyError(response))
      }
  }
}
