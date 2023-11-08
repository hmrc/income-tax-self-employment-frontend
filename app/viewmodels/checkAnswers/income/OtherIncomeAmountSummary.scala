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

import controllers.journeys.income.routes.OtherIncomeAmountController
import models.CheckMode
import models.database.UserAnswers
import pages.income.OtherIncomeAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OtherIncomeAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: Int, authUserType: String, businessId: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(OtherIncomeAmountPage, Some(businessId)).map { answer =>
      SummaryListRowViewModel(
        key = Key(content = s"otherIncomeAmount.checkYourAnswersLabel.$authUserType", classes = "govuk-!-width-two-thirds"),
        value = Value(content = s"£${formatMoney(answer)}", classes = "govuk-!-width-one-third"),
        actions = Seq(
          ActionItemViewModel("site.change", OtherIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("otherIncomeAmount.change.hidden"))
        )
      )
    }

}
