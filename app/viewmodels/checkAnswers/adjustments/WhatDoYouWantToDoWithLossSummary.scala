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

package viewmodels.checkAnswers.adjustments

import controllers.journeys.adjustments.profitOrLoss.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object WhatDoYouWantToDoWithLossSummary {

  def row(answers: UserAnswers, userType: UserType, taxYear: TaxYear, businessId: BusinessId, rightTextAlign: Boolean = true)(implicit
      messages: Messages): Option[SummaryListRow] =
    answers.get(WhatDoYouWantToDoWithLossPage, businessId) map { answers =>
      buildRowString(
        answers.map(answer => messages(s"whatDoYouWantToDoWithLoss.$answer")).mkString(", "),
        routes.CurrentYearLossController.onPageLoad(taxYear, businessId, CheckMode),
        messages(s"whatDoYouWantToDoWithLoss.title.$userType", taxYear.startYear.toString, taxYear.endYear.toString),
        messages("whatDoYouWantToDoWithLoss.change.hidden", taxYear.startYear.toString, taxYear.endYear.toString),
        rightTextAlign
      )
    }

}
