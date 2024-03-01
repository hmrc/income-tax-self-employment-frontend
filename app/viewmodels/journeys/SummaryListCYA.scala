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

package viewmodels.journeys

import models.common._
import models.database.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.AnswerSummary
import viewmodels.journeys.SummaryListCYA.summaryListOpt

case class SummaryListCYA(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages) {
  def mkSummaryList(rows: List[AnswerSummary]): SummaryList =
    summaryListOpt(rows.map(_.row(userAnswers, taxYear, businessId, userType)))
}

object SummaryListCYA {

  def summaryList(rows: List[SummaryListRow]): SummaryList = SummaryList(
    rows = rows,
    classes = "govuk-!-margin-bottom-7"
  )

  def summaryListOpt(rows: List[Option[SummaryListRow]]): SummaryList =
    summaryList(rows.flatten)

}
