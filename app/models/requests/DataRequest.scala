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

package models.requests

import controllers.actions.AuthenticatedIdentifierAction.User
import models.common.UserType
import models.database.UserAnswers
import play.api.mvc.{Request, WrappedRequest}

case class OptionalDataRequest[A](request: Request[A], userId: String, user: User, userAnswers: Option[UserAnswers])
    extends WrappedRequest[A](request) {
  val userType: UserType   = user.userType
  val answers: UserAnswers = userAnswers.getOrElse(UserAnswers(userId))
}

case class DataRequest[A](request: Request[A], userId: String, user: User, userAnswers: UserAnswers) extends WrappedRequest[A](request) {
  def userType: UserType = user.userType
}
