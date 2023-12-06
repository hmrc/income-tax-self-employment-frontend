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

package viewmodels.checkAnswers.income

import controllers.journeys.income.routes.TradingAllowanceController
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.income.TradingAllowancePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TradingAllowanceSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, userType: UserType, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TradingAllowancePage, Some(businessId)).map { answer =>
      val value = Value(content = HtmlContent(HtmlFormat.escape(messages(s"tradingAllowance.$answer"))), classes = "govuk-!-width-one-third")

      SummaryListRowViewModel(
        key = Key(content = s"tradingAllowance.checkYourAnswersLabel.$userType", classes = "govuk-!-width-two-thirds"),
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", TradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("tradingAllowance.change.hidden"))
        )
      )
    }

}
