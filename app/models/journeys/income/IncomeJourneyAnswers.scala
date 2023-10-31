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

package models.journeys.income

import models.HowMuchTradingAllowance.{LessThan, Maximum}
import models.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import models.{HowMuchTradingAllowance, TradingAllowance}
import play.api.libs.json.{Json, OFormat}

case class IncomeJourneyAnswers(incomeNotCountedAsTurnover: Boolean,
                                nonTurnoverIncomeAmount: Option[BigDecimal],
                                turnoverIncomeAmount: BigDecimal,
                                anyOtherIncome: Boolean,
                                otherIncomeAmount: Option[BigDecimal],
                                turnoverNotTaxable: Option[Boolean],
                                notTaxableAmount: Option[BigDecimal],
                                tradingAllowance: TradingAllowance,
                                howMuchTradingAllowance: Option[HowMuchTradingAllowance],
                                tradingAllowanceAmount: Option[BigDecimal]) {

  require(
    incomeNotCountedAsTurnoverValidation(this) &&
      anyOtherIncomeValidation(this) &&
      turnoverNotTaxableValidation(this) &&
      tradingAllowanceValidation(this) &&
      howMuchTradingAllowanceValidation(this),
    "provided case class parameters does not follow the allowed flow of pages"
  )
  /*
   * TODO We may want to factor in the affect of cash/accrual as an additional constraint as to what params are allowed,
   *  however this is not done here because the question of whether or not we want to add a cash/accrual flag to this
   *  case case class (or apply the constraint elsewhere) should consider if this would unnecessarily perturb the model
   *  for sending to our backend. If our back-end also needs this flag, consider putting it in here.
   */

  private def incomeNotCountedAsTurnoverValidation(incomeJourneyAnswers: IncomeJourneyAnswers): Boolean =
    if (incomeJourneyAnswers.incomeNotCountedAsTurnover) {
      incomeJourneyAnswers.nonTurnoverIncomeAmount.isDefined
    } else {
      incomeJourneyAnswers.nonTurnoverIncomeAmount.isEmpty
    }

  private def anyOtherIncomeValidation(incomeJourneyAnswers: IncomeJourneyAnswers): Boolean =
    if (incomeJourneyAnswers.anyOtherIncome) {
      incomeJourneyAnswers.otherIncomeAmount.isDefined
    } else {
      incomeJourneyAnswers.nonTurnoverIncomeAmount.isEmpty
    }

  private def turnoverNotTaxableValidation(incomeJourneyAnswers: IncomeJourneyAnswers): Boolean =
    incomeJourneyAnswers.turnoverNotTaxable match {
      case Some(isNotTaxable) => if (isNotTaxable) incomeJourneyAnswers.notTaxableAmount.isDefined else incomeJourneyAnswers.notTaxableAmount.isEmpty
      case None               => incomeJourneyAnswers.notTaxableAmount.isEmpty
    }

  private def tradingAllowanceValidation(incomeJourneyAnswers: IncomeJourneyAnswers): Boolean =
    incomeJourneyAnswers.tradingAllowance match {
      case UseTradingAllowance =>
        incomeJourneyAnswers.howMuchTradingAllowance.isDefined

      case DeclareExpenses =>
        incomeJourneyAnswers.howMuchTradingAllowance.isEmpty && incomeJourneyAnswers.tradingAllowanceAmount.isEmpty

    }

  private def howMuchTradingAllowanceValidation(incomeJourneyAnswers: IncomeJourneyAnswers): Boolean =
    incomeJourneyAnswers.howMuchTradingAllowance match {
      case i if i.contains(Maximum) || i.isEmpty => incomeJourneyAnswers.tradingAllowanceAmount.isEmpty
      case Some(LessThan)                        => incomeJourneyAnswers.tradingAllowanceAmount.isDefined
    }

}

object IncomeJourneyAnswers {
  implicit val formats: OFormat[IncomeJourneyAnswers] = Json.format[IncomeJourneyAnswers]
}
