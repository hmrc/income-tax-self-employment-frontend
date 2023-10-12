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

import play.api.libs.json.{Json, OFormat}

sealed trait HttpErrorBody

object HttpErrorBody {

  val parsingError: HttpErrorBody = SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR")

  /** Single Error * */
  case class SingleErrorBody(code: String, reason: String) extends HttpErrorBody

  /** Multiple Errors * */
  case class MultiErrorsBody(failures: Seq[SingleErrorBody]) extends HttpErrorBody

  object SingleErrorBody {
    implicit val formats: OFormat[SingleErrorBody] = Json.format[SingleErrorBody]
  }

  object MultiErrorsBody {
    implicit val formats: OFormat[MultiErrorsBody] = Json.format[MultiErrorsBody]
  }
}
