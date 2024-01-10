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

package viewmodels

import base.SpecBase
import cats.implicits._
import models.common.JourneyStatus._
import models.common.{JourneyStatus, TradingName}
import models.database.UserAnswers
import models.journeys.Journey._
import models.journeys.expenses.individualCategories._
import models.journeys.income.TradingAllowance
import models.journeys.{Journey, JourneyNameAndStatus}
import models.requests.TradesJourneyStatuses
import org.scalatest.TryValues._
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.expenses.tailoring.individualCategories._
import pages.income.TradingAllowancePage
import play.api.i18n.Messages
import viewmodels.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.TradeJourneyStatusesViewModelSpec._

import scala.util.Try

class TradeJourneyStatusesViewModelSpec extends SpecBase with TableDrivenPropertyChecks {
  private implicit val messages: Messages = messagesStubbed

  private val categoriesExpenses = List[UserAnswers => Try[UserAnswers]](
    _.set(FinancialExpensesPage, Set[FinancialExpenses](FinancialExpenses.IrrecoverableDebts), businessId.some),
    _.set(DisallowableIrrecoverableDebtsPage, DisallowableIrrecoverableDebts.Yes, businessId.some),
    _.set(TradingAllowancePage, TradingAllowance.DeclareExpenses, businessId.some),
    _.set(OfficeSuppliesPage, OfficeSupplies.YesAllowable, businessId.some),
    _.set(GoodsToSellOrUsePage, GoodsToSellOrUse.YesDisallowable, businessId.some)
  )

  private val testScenarios = Table(
    ("JourneyNameAndStatus", "userAnswers", "expected"),
    // No statuses, no answers
    (
      Nil,
      Nil,
      List(
        expectedRow("/2024/SJPR05893938418/details/self-employment-abroad", "", Journey.Abroad, NotStarted),
        expectedRow("#", " class='govuk-deadlink'", Journey.Income, CannotStartYet)
      )),
    // Just one route: Abroad in progress
    (
      List(JourneyNameAndStatus(Abroad, InProgress)),
      Nil,
      List(
        expectedRow("/2024/SJPR05893938418/self-employment/details/check", "", Journey.Abroad, InProgress),
        expectedRow("#", " class='govuk-deadlink'", Journey.Income, CannotStartYet)
      )),
    // Abroad, Income there, but income without answers so no expenses tailoring rendered
    (
      List(
        JourneyNameAndStatus(Abroad, Completed),
        JourneyNameAndStatus(Income, CheckOurRecords),
        JourneyNameAndStatus(ExpensesTailoring, Completed)
      ),
      Nil,
      List(
        expectedRow("/2024/SJPR05893938418/self-employment/details/check", "", Journey.Abroad, Completed),
        expectedRow("/2024/SJPR05893938418/income/not-counted-turnover", "", Journey.Income, CheckOurRecords)
      )),
    // Expense Tailoring there, with some sub journeys
    (
      List(
        JourneyNameAndStatus(Abroad, Completed),
        JourneyNameAndStatus(Income, Completed),
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(ExpensesOfficeSupplies, CheckOurRecords),
        JourneyNameAndStatus(ExpensesGoodsToSellOrUse, InProgress)
      ),
      categoriesExpenses,
      List(
        expectedRow("/2024/SJPR05893938418/self-employment/details/check", "", Journey.Abroad, Completed),
        expectedRow("/2024/SJPR05893938418/income/check-your-income", "", Journey.Income, Completed),
        expectedRow("/2024/SJPR05893938418/expenses/check", "", Journey.ExpensesTailoring, Completed),
        expectedRow("/2024/SJPR05893938418/expenses/office-supplies/amount", "", Journey.ExpensesOfficeSupplies, CheckOurRecords),
        expectedRow("/2024/SJPR05893938418/expenses/goods-sell-use/check", "", Journey.ExpensesGoodsToSellOrUse, InProgress),
        expectedRow("/2024/SJPR05893938418/expenses/irrecoverable-debts/amount", "", Journey.ExpensesIrrecoverableDebts, NotStarted)
      )
    )
  )

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (journeyCompletedStates, answers, expectedRows) =>
        val userAnswers           = buildAnswers(answers)
        val tradesJourneyStatuses = TradesJourneyStatuses(businessId, Some(TradingName("tradingName")), journeyCompletedStates)
        val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear, Some(userAnswers))

        withClue(s"""
            |${result.rows.mkString("\n")}
            |did not equal to:
            |${expectedRows.mkString("\n")}
            |""".stripMargin) {
          assert(result.rows === expectedRows)
        }
      }
    }
  }

}

object TradeJourneyStatusesViewModelSpec {

  def buildAnswers(setOps: List[UserAnswers => Try[UserAnswers]]): UserAnswers =
    setOps
      .foldRight(Try(SpecBase.emptyUserAnswers)) { (setAnswer, currentAnswers) =>
        setAnswer(currentAnswers.success.value)
      }
      .success
      .value

  def expectedRow(href: String, optDeadlinkStyle: String, journey: Journey, status: JourneyStatus)(implicit messages: Messages) =
    buildSummaryRow(href, optDeadlinkStyle, s"journeys.$journey", status)

}
