/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses.travelAndAccommodation

import controllers.journeys.expenses.travelAndAccommodation.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.CostsNotCoveredPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString
import utils.MoneyUtils.formatMoney

object CostsNotCoveredSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CostsNotCoveredPage, businessId).map { answer =>
      buildRowString(
        s"£${formatMoney(answer)}",
        callLink = routes.CostsNotCoveredController.onPageLoad(taxYear, businessId, CheckMode),
        keyMessage = messages(s"costsNotCovered.subheading.$userType"),
        changeMessage = s"travelForWorkYourVehicle.change.hidden.$userType",
        rightTextAlign = true
      )
    }
}
