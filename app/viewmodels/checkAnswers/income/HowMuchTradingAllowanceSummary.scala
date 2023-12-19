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
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.income.HowMuchTradingAllowance
import pages.income.{HowMuchTradingAllowancePage, TurnoverIncomeAmountPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowString

object HowMuchTradingAllowanceSummary extends MoneyUtils {

  def row(userAnswers: UserAnswers, taxYear: TaxYear, userType: UserType, businessId: BusinessId)(implicit
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
      } yield buildRowString(
        rowValue,
        HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode),
        s"howMuchTradingAllowance.checkYourAnswersLabel.$userType",
        "howMuchTradingAllowance.change.hidden"
      )
    }

  private def calculateMaxTradingAllowance(userAnswers: UserAnswers, businessId: BusinessId): Either[Exception, String] =
    userAnswers.get(TurnoverIncomeAmountPage, Some(businessId)) match {
      case Some(amount) if amount < 1000  => Right(formatMoney(amount))
      case Some(amount) if amount >= 1000 => Right(formatMoney(1000))
      case _                              => Left(new RuntimeException("Unable to retrieve user answers for TurnoverIncomeAmountPage"))
    }

}
