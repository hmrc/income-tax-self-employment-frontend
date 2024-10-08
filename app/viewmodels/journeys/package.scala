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

package viewmodels

import models.common.JourneyStatus.{CannotStartYet, CheckOurRecords, Completed, InProgress, NotStarted}
import models.common.{BusinessId, Journey, JourneyStatus}
import models.database.UserAnswers
import models.journeys.JourneyNameAndStatus
import models.requests.TradesJourneyStatuses
import pages.OneQuestionPage
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

package object journeys {

  def isJourneyCompletedOrInProgress(tradesJourneyStatuses: TradesJourneyStatuses, dependentJourney: Journey): Boolean =
    getJourneyStatus(dependentJourney)(tradesJourneyStatuses.journeyStatuses) match {
      case Completed | InProgress                        => true
      case CheckOurRecords | CannotStartYet | NotStarted => false
    }

  def getJourneyStatus(journey: Journey, dependentJourneyIsFinishedForClickableLink: Boolean = true)(implicit
      journeyStatuses: List[JourneyNameAndStatus]): JourneyStatus =
    if (dependentJourneyIsFinishedForClickableLink) JourneyStatus.getJourneyStatus(journey, journeyStatuses) else CannotStartYet

  def getPageAnswer[A](page: OneQuestionPage[A])(implicit businessId: BusinessId, userAnswers: Option[UserAnswers], reads: Reads[A]): Option[A] =
    userAnswers.flatMap(_.get(page, Some(businessId)))

  def determineJourneyStartOrCyaUrl(startUrl: String, cyaUrl: String)(implicit status: JourneyStatus): String =
    status match {
      case CannotStartYet               => "#"
      case Completed | InProgress       => cyaUrl
      case NotStarted | CheckOurRecords => startUrl
    }

  def returnRowIfConditionPassed(row: SummaryListRow, conditionIsPassed: Boolean): Option[SummaryListRow] =
    if (conditionIsPassed) Some(row) else None

  def conditionPassedForViewableLink[A](page: OneQuestionPage[A], acceptableAnswers: Seq[A])(implicit
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      readsA: Reads[A]): Boolean =
    getPageAnswer(page).fold(false)(acceptableAnswers.contains(_))

  def conditionPassedForViewableLink[A](page: OneQuestionPage[Set[A]], requiredAnswer: A)(implicit
      businessId: BusinessId,
      userAnswers: Option[UserAnswers],
      readsA: Reads[A]): Boolean =
    getPageAnswer(page).fold(false)(_.contains(requiredAnswer))
}
