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
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.income.HowMuchTradingAllowance
import pages.income.{HowMuchTradingAllowancePage, TurnoverIncomeAmountPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HowMuchTradingAllowanceSummary extends MoneyUtils {

  def row(userAnswers: UserAnswers, taxYear: TaxYear, authUserType: String, businessId: BusinessId)(implicit
      messages: Messages): Option[Either[Exception, SummaryListRow]] =
    userAnswers.get(HowMuchTradingAllowancePage, Some(businessId)).map { answer =>
      val rowValueOrError = answer match {
        case HowMuchTradingAllowance.Maximum =>
          calculateMaxTradingAllowance(userAnswers, businessId)
            .map(amount => s"The maximum £$amount")

        case HowMuchTradingAllowance.LessThan =>
          Right(messages("howMuchTradingAllowance.lowerAmount"))
      }

      for {
        rowValue <- rowValueOrError
      } yield SummaryListRowViewModel(
        key = Key(content = s"howMuchTradingAllowance.checkYourAnswersLabel.$authUserType", classes = "govuk-!-width-two-thirds"),
        value = Value(content = rowValue, classes = "govuk-!-width-one-third"),
        actions = Seq(
          ActionItemViewModel("site.change", HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("howMuchTradingAllowance.change.hidden")))
      )
    }

  private def calculateMaxTradingAllowance(userAnswers: UserAnswers, businessId: BusinessId): Either[Exception, String] =
    userAnswers.get(TurnoverIncomeAmountPage, Some(businessId)) match {
      case Some(amount) if amount < 1000  => Right(formatMoney(amount))
      case Some(amount) if amount >= 1000 => Right(formatMoney(1000))
      case _                              => Left(new RuntimeException("Unable to retrieve user answers for TurnoverIncomeAmountPage"))
    }

}
