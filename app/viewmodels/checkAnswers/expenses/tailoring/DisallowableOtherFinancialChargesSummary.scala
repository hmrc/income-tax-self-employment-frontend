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

package viewmodels.checkAnswers.expenses.tailoring

import controllers.journeys.expenses.tailoring.routes.DisallowableOtherFinancialChargesController
import models.CheckMode
import models.common.TaxYear
import models.database.UserAnswers
import pages.expenses.tailoring.DisallowableOtherFinancialChargesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DisallowableOtherFinancialChargesSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DisallowableOtherFinancialChargesPage).map { answer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"disallowableOtherFinancialCharges.$answer"))
        )
      )

      SummaryListRowViewModel(
        key = "disallowableOtherFinancialCharges.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("disallowableOtherFinancialCharges.change.hidden"))
        )
      )
    }

}
