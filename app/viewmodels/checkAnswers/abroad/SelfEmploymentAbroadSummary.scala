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

package viewmodels.checkAnswers.abroad

import controllers.journeys.abroad.routes.SelfEmploymentAbroadController
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.abroad.SelfEmploymentAbroadPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowBoolean

object SelfEmploymentAbroadSummary {

  def row(taxYear: TaxYear, userType: UserType, businessId: BusinessId, userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow =
    userAnswers.get(SelfEmploymentAbroadPage, Some(businessId)) match {
      case Some(answer) =>
        buildRowBoolean(
          answer,
          SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, CheckMode),
          s"selfEmploymentAbroad.title.$userType",
          "selfEmploymentAbroad.change.hidden"
        )

      case None => throw new RuntimeException("No UserAnswers retrieved for SelfEmploymentAbroadPage")
    }

}
