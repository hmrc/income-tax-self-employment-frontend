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

package pages

import controllers.standard
import models._
import models.common._
import models.database.UserAnswers
import play.api.libs.json.Reads
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import queries.{Gettable, Settable}

trait QuestionPage[A] extends Page with Gettable[A] with Settable[A] {

  def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = ???

  def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call = ???

  def standardPage: Call =
    standard.routes.JourneyRecoveryController.onPageLoad()

  def redirectNext(mode: Mode, userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Result = {
    val newPage: Call = mode match {
      case NormalMode => nextPageInNormalMode(userAnswers, businessId, taxYear)
      case CheckMode  => cyaPage(taxYear, businessId)
    }

    Redirect(newPage)
  }

  def redirectOnBoolean(userAnswers: UserAnswers, businessId: BusinessId, onTrue: => Call, onFalse: => Call)(implicit reads: Reads[A]): Call =
    userAnswers
      .get(this, businessId)
      .fold(standardPage) {
        case true  => onTrue
        case false => onFalse
      }

}
