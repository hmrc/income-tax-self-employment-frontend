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

package viewmodels.checkAnswers.expenses.staffCosts

import controllers.journeys.expenses.staffCosts.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import pages.expenses.staffCosts.StaffCostsAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowBigDecimal

object StaffCostsAmountSummary {

  def row(request: DataRequest[_], taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    request.getValue(StaffCostsAmountPage, businessId).map { answer =>
      buildRowBigDecimal(
        answer,
        routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, CheckMode),
        s"staffCostsAmount.title.${request.userType}",
        "staffCostsAmount.change.hidden"
      )
    }

}
