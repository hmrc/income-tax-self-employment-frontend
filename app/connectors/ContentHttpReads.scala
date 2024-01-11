/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.implicits._
import connectors.ContentHttpReads._
import connectors.httpParser.HttpParser.unsafePagerDutyError
import models.errors.ServiceError
import models.errors.ServiceError.{CannotParseJsonError, CannotReadJsonError}
import play.api.libs.json.{JsObject, Json, Reads, Writes}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.{Failure, Success, Try}

class ContentHttpReads[A: Reads] extends HttpReads[ContentResponse[A]] {

  override def read(method: String, url: String, response: HttpResponse): ContentResponse[A] =
    if (isSuccess(response.status)) {
      readOne[A](response)
    } else {
      Left(unsafePagerDutyError(method, url, response))
    }
}

object ContentHttpReads {
  def readOne[A: Reads](response: HttpResponse): Either[ServiceError, A] = {
    val validated = Try(response.json.validate[A].asEither)

    validated match {
      case Success(validatedRes) =>
        validatedRes.fold(
          err => CannotReadJsonError(err.toList).asLeft,
          a => a.asRight
        )

      case Failure(err) => CannotParseJsonError(err).asLeft
    }
  }

  def asJsonUnsafe[A: Writes](a: A): JsObject = Json.toJson(a).as[JsObject]
}
