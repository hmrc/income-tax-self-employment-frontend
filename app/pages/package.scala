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

import models.common.BusinessId
import models.database.UserAnswers
import play.api.libs.json.Reads
import play.api.mvc.Call
import queries.Gettable
import controllers.standard

package object pages {
  def standardPage: Call =
    standard.routes.JourneyRecoveryController.onPageLoad()

  def redirectOnBoolean(page: Gettable[Boolean], userAnswers: UserAnswers, businessId: BusinessId, onTrue: => Call, onFalse: => Call)(implicit
      reads: Reads[Boolean]): Call =
    userAnswers
      .get(page, businessId)
      .fold(standardPage) {
        case true  => onTrue
        case false => onFalse
      }
}
