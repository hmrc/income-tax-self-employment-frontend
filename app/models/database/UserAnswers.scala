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

package models.database

import models.RichJsObject
import models.common.{BusinessId, UserId}
import play.api.libs.json._
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(id: String, data: JsObject = Json.obj(), lastUpdated: Instant = Instant.now) {
  val isEmpty: Boolean = data.fields.isEmpty

  def get[A](page: Gettable[A], businessId: Option[BusinessId] = None)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path(businessId))).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A, businessId: Option[BusinessId] = None)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path(businessId), Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(updatedAnswers)
    }
  }

  def upsertFragment(businessId: BusinessId, dataFragment: JsObject): UserAnswers = {
    val existingAnswerData = (data \ businessId.value).asOpt[JsObject].getOrElse(JsObject.empty)
    val updatedData        = data + (businessId.value -> (existingAnswerData ++ dataFragment))
    copy(data = updatedData, lastUpdated = Instant.now)
  }

  def remove[A](page: Settable[A], businessId: Option[BusinessId] = None): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path(businessId)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(updatedAnswers)
    }
  }

}

object UserAnswers {

  def empty(userId: UserId): UserAnswers = UserAnswers(userId.value)

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
