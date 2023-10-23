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

import controllers.journeys.income.routes.HowMuchTradingAllowanceController
import models.{CheckMode, HowMuchTradingAllowance, UserAnswers}
import pages.income.{HowMuchTradingAllowancePage, TurnoverIncomeAmountPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HowMuchTradingAllowanceSummary {

  def row(answers: UserAnswers, taxYear: Int, authUserType: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowMuchTradingAllowancePage).map { answer =>
      val value = answer match {
        case HowMuchTradingAllowance.Maximum =>
          val turnoverIncomeAmount = answers.get(TurnoverIncomeAmountPage) match {
            case Some(amount) => amount
            case None         => throw new RuntimeException("Unable to retrieve user answers for TurnoverIncomeAmountPage")
          }
          s"The maximum £$turnoverIncomeAmount"

        case HowMuchTradingAllowance.LessThan => messages("howMuchTradingAllowance.lowerAmount")
      }
      SummaryListRowViewModel(
        key = Key(content = s"howMuchTradingAllowance.checkYourAnswersLabel.$authUserType", classes = "govuk-!-width-two-thirds"),
        value = Value(content = value, classes = "govuk-!-width-one-third"),
        actions = Seq(
          ActionItemViewModel("site.change", HowMuchTradingAllowanceController.onPageLoad(taxYear, CheckMode).url)
            .withVisuallyHiddenText(messages("howMuchTradingAllowance.change.hidden"))
        )
      )
    }

}
