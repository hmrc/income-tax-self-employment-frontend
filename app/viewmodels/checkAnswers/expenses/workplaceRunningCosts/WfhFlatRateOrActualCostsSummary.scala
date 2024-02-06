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

package viewmodels.checkAnswers.expenses.workplaceRunningCosts

import controllers.journeys.expenses.workplaceRunningCosts.workingFromHome.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object WfhFlatRateOrActualCostsSummary {


  def row(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
                                                                                                  messages: Messages): Option[SummaryListRow] =
    userAnswers.get(WfhFlatRateOrActualCostsPage, Some(businessId)).map { answer =>
      buildRowString(
        answer.toString,
        routes.WfhFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, CheckMode),
        messages(s"wfhFlatRateOrActualCosts.subHeading.$userType", answer), // TODO change to flat rate view model
        "wfhFlatRateOrActualCosts.change.hidden",
        rightTextAlign = true
      )
    }


}
