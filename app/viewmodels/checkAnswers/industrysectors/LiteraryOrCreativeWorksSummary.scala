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

package viewmodels.checkAnswers.industrysectors

import controllers.journeys.industrysectors.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.journeys.industrySectors.IndustrySectorsDb
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object LiteraryOrCreativeWorksSummary {

  def row(taxYear: TaxYear, businessId: BusinessId, model: IndustrySectorsDb)(implicit messages: Messages): Option[SummaryListRow] =
    model.hasProfitFromCreativeWorks.map { answer =>
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "literaryOrCreativeWorks.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.LiteraryOrCreativeWorksController.onPageLoad(taxYear, businessId = businessId, mode = CheckMode).url)
            .withVisuallyHiddenText(messages("literaryOrCreativeWorks.change.hidden"))
        )
      )
    }
}
