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

package viewmodels.checkAnswers.expenses.goodsToSellOrUse

import controllers.journeys.expenses.goodsToSellOrUse.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.goodsToSellOrUse.TaxiMinicabOrRoadHaulagePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowBoolean

object TaxiMinicabOrRoadHaulageSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TaxiMinicabOrRoadHaulagePage, Some(businessId)).map { answer =>
      buildRowBoolean(
        answer,
        routes.TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, businessId, CheckMode),
        s"taxiMinicabOrRoadHaulage.title.$userType",
        "taxiMinicabOrRoadHaulage.change.hidden",
        rightTextAlign = true
      )
    }

}
