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

package models.journeys.income

object IncomeJourneyAnswersTestData {
  val sample: IncomeJourneyAnswers = IncomeJourneyAnswers(
    incomeNotCountedAsTurnover = true,
    nonTurnoverIncomeAmount = Some(1.0),
    turnoverIncomeAmount = 2.0,
    anyOtherIncome = true,
    otherIncomeAmount = Some(3.0),
    turnoverNotTaxable = Some(true),
    notTaxableAmount = Some(4.0),
    tradingAllowance = TradingAllowance.DeclareExpenses,
    howMuchTradingAllowance = Some(HowMuchTradingAllowance.Maximum),
    tradingAllowanceAmount = Some(5.0)
  )
}
