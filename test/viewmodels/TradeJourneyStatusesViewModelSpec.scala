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
import controllers.journeys._
import models._
import models.common.AccountingType.Accrual
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
import viewmodels.TradeJourneyStatusesViewModelSpec._
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

import scala.util.Try

class TradeJourneyStatusesViewModelSpec extends SpecBase with TableDrivenPropertyChecks {
  private implicit val messages: Messages = messagesStubbed

  private val categoriesExpenses = List[UserAnswers => Try[UserAnswers]](
    _.set(FinancialExpensesPage, Set[FinancialExpenses](FinancialExpenses.IrrecoverableDebts), businessId.some),
    _.set(DisallowableIrrecoverableDebtsPage, DisallowableIrrecoverableDebts.Yes, businessId.some),
    _.set(TradingAllowancePage, TradingAllowance.DeclareExpenses, businessId.some),
    _.set(OfficeSuppliesPage, OfficeSupplies.YesAllowable, businessId.some),
    _.set(GoodsToSellOrUsePage, GoodsToSellOrUse.YesDisallowable, businessId.some),
    _.set(OtherExpensesPage, OtherExpenses.YesDisallowable, businessId.some)
  )

  private val abroadUrl    = abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
  private val abroadCyaUrl = abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
  private val incomeUrl    = income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url
  private val incomeCyaUrl = income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
  private val capitalAllowancesUrl =
    capitalallowances.tailoring.routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode).url
  private val expensesTailoringCyaUrl = expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
  private val officeSuppliesUrl       = expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  private val goodsToSellCyaUrl       = expenses.goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
  private val irrecoverableUrl = expenses.irrecoverableDebts.routes.IrrecoverableDebtsAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  private val otherExpensesUrl = expenses.otherExpenses.routes.OtherExpensesAmountController.onPageLoad(taxYear, businessId, NormalMode).url

  private val testScenarios = Table(
    ("JourneyNameAndStatus", "userAnswers", "expected"),
    // No statuses, no answers
    (
      Nil,
      Nil,
      List(
        expectedRow(abroadUrl, Abroad, NotStarted),
        expectedRow("#", Income, CannotStartYet),
        expectedRow("#", CapitalAllowancesTailoring, CannotStartYet)
      )),
    // Just one route: Abroad in progress
    (
      List(JourneyNameAndStatus(Abroad, InProgress)),
      Nil,
      List(
        expectedRow(abroadCyaUrl, Abroad, InProgress),
        expectedRow("#", Income, CannotStartYet),
        expectedRow("#", CapitalAllowancesTailoring, CannotStartYet)
      )),
    // Abroad, Income and CapAllowances there, but income without answers so no expenses tailoring rendered
    (
      List(
        JourneyNameAndStatus(Abroad, Completed),
        JourneyNameAndStatus(Income, NotStarted),
        JourneyNameAndStatus(CapitalAllowancesTailoring, NotStarted),
        JourneyNameAndStatus(ExpensesTailoring, Completed)
      ),
      Nil,
      List(
        expectedRow(abroadCyaUrl, Abroad, Completed),
        expectedRow(incomeUrl, Income, NotStarted),
        expectedRow(capitalAllowancesUrl, CapitalAllowancesTailoring, NotStarted)
      )),
    // Expense Tailoring there, with some sub journeys
    (
      List(
        JourneyNameAndStatus(Abroad, Completed),
        JourneyNameAndStatus(Income, Completed),
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(ExpensesOfficeSupplies, NotStarted),
        JourneyNameAndStatus(ExpensesGoodsToSellOrUse, InProgress),
        JourneyNameAndStatus(CapitalAllowancesTailoring, NotStarted)
      ),
      categoriesExpenses,
      List(
        expectedRow(abroadCyaUrl, Abroad, Completed),
        expectedRow(incomeCyaUrl, Income, Completed),
        expectedRow(expensesTailoringCyaUrl, ExpensesTailoring, Completed),
        expectedRow(officeSuppliesUrl, ExpensesOfficeSupplies, NotStarted),
        expectedRow(goodsToSellCyaUrl, ExpensesGoodsToSellOrUse, InProgress),
        expectedRow(irrecoverableUrl, ExpensesIrrecoverableDebts, NotStarted),
        expectedRow(otherExpensesUrl, ExpensesOtherExpenses, NotStarted),
        expectedRow(capitalAllowancesUrl, CapitalAllowancesTailoring, NotStarted)
      )
    )
  )

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (journeyCompletedStates, answers, expectedRows) =>
        val userAnswers           = buildAnswers(answers)
        val tradesJourneyStatuses = TradesJourneyStatuses(businessId, Some(TradingName("tradingName")), Accrual, journeyCompletedStates)
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
      .foldRight(Try(SpecBase.emptyUserAnswersAccrual)) { (setAnswer, currentAnswers) =>
        setAnswer(currentAnswers.success.value)
      }
      .success
      .value

  def expectedRow(href: String, journey: Journey, status: JourneyStatus)(implicit messages: Messages) =
    buildSummaryRow(href, s"journeys.$journey", status)

}
