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

package models.errors

import cats.implicits._
import models.errors.HttpErrorBody.SingleErrorBody
import play.api.libs.json.{JsPath, JsonValidationError}
import play.mvc.Http.Status

/** Our common base for all errors all over the layers. Use internalReason Right if there is no exception with the error details, or Left if there is
  * an exception.
  */
sealed abstract class ServiceError(status: Int, internalReason: Either[Throwable, String]) {
  private val errorMessage = internalReason.left.map(_.getMessage).merge
  private val reasonForUser: HttpErrorBody =
    if (status >= 500 && status <= 599) HttpErrorBody.internalError(errorMessage)
    else SingleErrorBody("NOT_FOUND", errorMessage)

  def httpError: HttpError =
    HttpError(status, reasonForUser, internalReason.left.toOption, errorMessage.some)
}

object ServiceError {
  type JsonErrorWithPath = List[(JsPath, scala.collection.Seq[JsonValidationError])]

  /** Use for code which has not yet been refactored to user Service Error and use HttpError directly */
  final case class ConnectorResponseError(originalHttpError: HttpError) extends ServiceError(originalHttpError.status, Right("internal error")) {
    override def httpError: HttpError = originalHttpError
  }

  final case class CannotReadJsonError(details: JsonErrorWithPath)
      extends ServiceError(Status.INTERNAL_SERVER_ERROR, s"Cannot read JSON: ${details.toString}".asRight)

  final case class CannotParseJsonError(details: Throwable)
      extends ServiceError(Status.INTERNAL_SERVER_ERROR, s"Cannot prase JSON: ${details.toString}".asRight)

  final case class CannotUpsertToMongoError(reason: Throwable)
      extends ServiceError(Status.INTERNAL_SERVER_ERROR, s"Cannot upsert to mongo: ${reason.getMessage}".asRight)

  final case class NotFoundError(reason: String) extends ServiceError(Status.NOT_FOUND, reason.asRight)
}
