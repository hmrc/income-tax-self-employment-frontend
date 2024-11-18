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

package viewmodels.checkAnswers.capitalallowances.specialTaxSites

import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowBigDecimal

object QualifyingExpenditureSummary {

  def row(answer: BigDecimal, taxYear: TaxYear, businessId: BusinessId, userType: UserType, index: Int)(implicit messages: Messages): SummaryListRow =
    buildRowBigDecimal(
      answer,
      routes.QualifyingExpenditureController.onPageLoad(taxYear, businessId, index, CheckMode),
      messages(s"qualifyingExpenditure.cya.$userType"),
      "qualifyingExpenditure.change.hidden"
    )
}
