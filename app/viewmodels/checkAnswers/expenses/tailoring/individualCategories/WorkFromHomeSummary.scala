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

package viewmodels.checkAnswers.expenses.tailoring.individualCategories

import controllers.journeys.expenses.tailoring.individualCategories.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.tailoring.individualCategories.WorkFromHomePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRow
import viewmodels.checkAnswers.expenses.tailoring.formatAnswer

object WorkFromHomeSummary {

  def row()(implicit messages: Messages, answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType): Option[SummaryListRow] =
    answers.get(WorkFromHomePage, Some(businessId)).map { answer =>
      buildRow(
        formatAnswer(answer.toString),
        routes.WorkFromHomeController.onPageLoad(taxYear, businessId, CheckMode),
        s"workFromHome.title.$userType",
        "workFromHome.change.hidden"
      )
    }

}
