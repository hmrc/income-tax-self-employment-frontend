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

import controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.LiveAtBusinessPremisesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object LiveAtBusinessPremisesSummary {


  def row(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
                                                                                                  messages: Messages): Option[SummaryListRow] =
    userAnswers.get(LiveAtBusinessPremisesPage, Some(businessId)).map { answer =>
      buildRowString(
        answer.toString,
        routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, CheckMode),
        s"liveAtBusinessPremises.title.$userType",
        "liveAtBusinessPremises.change.hidden",
        rightTextAlign = true
      )
    }


}
