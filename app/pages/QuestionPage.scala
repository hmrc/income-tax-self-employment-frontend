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
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import queries.{Gettable, Settable}
import utils.Logging

import scala.concurrent.Future

trait QuestionPage[A] extends Page with Gettable[A] with Settable[A] with Logging {

  // TODO Remove ??? once all pages use this pattern
  def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = ???

  def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call = ???

  def cyaPage(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call = ???

  private def recoveryPage: Call = standard.routes.JourneyRecoveryController.onPageLoad()

  def redirectToRecoveryPage(reason: String): Result = {
    logger.warn(s"Redirect to recovery page. Reason: $reason")
    Redirect(recoveryPage)
  }

  def redirectToRecoveryPageF(reason: String): Future[Result] = {
    logger.warn(s"Redirect to recovery page. Reason: $reason")
    Future.successful(Redirect(recoveryPage))
  }

  /** Pages which needs to be cleared when No is selected in the main page */
  val dependentPagesWhenNo: List[Settable[_]] = Nil

  /** Pages which needs to be cleared when Yes is selected in the main page */
  val dependentPagesWhenYes: List[Settable[_]] = Nil

  /** Pages which need to be cleared when a non Boolean page's answer changes */
  val dependentPagesWhenAnswerChanges: List[Settable[_]] = Nil

  val requiredErrorKey: String = s"$toString.error.required"

  def redirectNext(originalMode: Mode,
                   userAnswers: UserAnswers,
                   businessId: BusinessId,
                   taxYear: TaxYear,
                   cyaPageWithUserAnswers: Boolean = false): Result = {
    val updatedMode = if (hasAllFurtherAnswers(businessId, userAnswers)) originalMode else NormalMode
    val newPage: Call = updatedMode match {
      case NormalMode => nextPageInNormalMode(userAnswers, businessId, taxYear)
      case CheckMode  => if (cyaPageWithUserAnswers) cyaPage(userAnswers, taxYear, businessId) else cyaPage(taxYear, businessId)
    }

    Redirect(newPage)
  }

  /** You can use this method to determine if all answers are provided starting from a page. The value is overridden in each of the page
    */
  def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean = {
    val _ = (businessId, userAnswers) // just to remove unused warning
    false
  }

  def next(userAnswers: UserAnswers, businessId: BusinessId): Option[PageJourney] = {
    val _ = (userAnswers, businessId) // just to remove 'unused' warning
    None
  }

}
