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

package queries

import cats.implicits.catsSyntaxOptionId
import models.common.BusinessId
import models.database.UserAnswers
import play.api.libs.json.{JsPath, Writes}

import scala.util.{Success, Try}

sealed trait Query {

  def path(businessId: Option[BusinessId]): JsPath
}

trait Gettable[A] extends Query

trait Settable[A] extends Query {

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    Success(userAnswers)
}

object Settable {
  final case class SetAnswer[A: Writes](page: Settable[A], value: A) {
    def set(userAnswers: UserAnswers, businessId: BusinessId): Try[UserAnswers] = userAnswers.set(page, value, businessId.some)
  }

  object SetAnswer {
    def setMany(businessId: BusinessId, userAnswers: UserAnswers)(commands: SetAnswer[_]*): Try[UserAnswers] =
      commands.foldLeft(Try(userAnswers)) { (acc, command) =>
        acc.flatMap(command.set(_, businessId))
      }
  }
}
