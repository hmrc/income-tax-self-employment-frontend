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
import controllers.journeys
import models.NormalMode
import models.common.JourneyStatus
import models.common.JourneyStatus._
import models.database.UserAnswers
import models.journeys.Journey._
import models.journeys.expenses.individualCategories._
import models.journeys.income.TradingAllowance
import models.journeys.{Journey, JourneyNameAndStatus}
import models.requests.TradesJourneyStatuses
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.expenses.tailoring.individualCategories._
import pages.income.TradingAllowancePage
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

// TODO This test is really hard to debug. It has too much logic, so cannot be trusted fully
// Refactor to be Table(actual, expected)
class TradeJourneyStatusesViewModelSpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  private val officeSuppliesAndGTSOUYes = emptyUserAnswers
    .set(OfficeSuppliesPage, OfficeSupplies.YesAllowable, Some(businessId))
    .success
    .value
    .set(GoodsToSellOrUsePage, GoodsToSellOrUse.YesDisallowable, Some(businessId))
    .success
    .value

  private val testScenarios = Table(
    ("JourneyNameAndStatus", "userAnswers"),
    (Nil, emptyUserAnswers),
    (List(JourneyNameAndStatus(Abroad, JourneyStatus.InProgress)), emptyUserAnswers),
    (
      List(
        JourneyNameAndStatus(Abroad, JourneyStatus.Completed),
        JourneyNameAndStatus(Income, JourneyStatus.CheckOurRecords),
        JourneyNameAndStatus(ExpensesTailoring, JourneyStatus.Completed)
      ),
      emptyUserAnswers
    ),
    (
      List(
        JourneyNameAndStatus(Abroad, JourneyStatus.Completed),
        JourneyNameAndStatus(Income, JourneyStatus.Completed),
        JourneyNameAndStatus(ExpensesTailoring, JourneyStatus.Completed),
        JourneyNameAndStatus(ExpensesOfficeSupplies, JourneyStatus.CheckOurRecords),
        JourneyNameAndStatus(ExpensesGoodsToSellOrUse, JourneyStatus.InProgress)
      ),
      officeSuppliesAndGTSOUYes
    )
  ) // TODO find a better solution for generating combinations of different journey states and user answers

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (journeyCompletedStates, userAnswers) =>
        val tradesJourneyStatuses = TradesJourneyStatuses(businessId, Some("tradingName"), journeyCompletedStates)
        val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear, Some(userAnswers))

        val expected = buildExpectedResult(journeyCompletedStates, userAnswers)
        result.rows.map(_.toString) mustEqual expected
      }
    }
  }

  private def buildExpectedResult(journeyCompletedStates: List[JourneyNameAndStatus], userAnswers: UserAnswers): Seq[String] = {
    val tailoringCyaIsAnswered = findJourneyStatus(journeyCompletedStates, ExpensesTailoring) match {
      case InProgress | Completed => true
      case _                      => false
    }
    val abroadStatus          = findJourneyStatus(journeyCompletedStates, Abroad)
    val incomeStatus          = findJourneyStatus(journeyCompletedStates, Income)
    val declareExpenses       = userAnswers.get(TradingAllowancePage, Some(businessId)).contains(TradingAllowance.DeclareExpenses)
    val officeSuppliesIsYes   = tailoringCyaIsAnswered && userAnswers.get(OfficeSuppliesPage, Some(businessId)).exists(_ != OfficeSupplies.No)
    val goodsToSellOrUseIsYes = tailoringCyaIsAnswered && userAnswers.get(GoodsToSellOrUsePage, Some(businessId)).exists(_ != GoodsToSellOrUse.No)
    val repairsAndMaintenanceIsYes =
      tailoringCyaIsAnswered && userAnswers.get(RepairsAndMaintenancePage, Some(businessId)).exists(_ != RepairsAndMaintenance.No)
    val advertisingIsYes =
      tailoringCyaIsAnswered && userAnswers.get(AdvertisingOrMarketingPage, Some(businessId)).exists(_ != AdvertisingOrMarketing.No)
    val entertainmentsIsYes = tailoringCyaIsAnswered && userAnswers.get(EntertainmentCostsPage, Some(businessId)).exists(_ != EntertainmentCosts.No)
    val staffCostsIsYes =
      tailoringCyaIsAnswered && userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)).contains(ProfessionalServiceExpenses.Staff)
    val constructionIsYes =
      tailoringCyaIsAnswered && userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)).contains(ProfessionalServiceExpenses.Construction)
    val professionalFeesIsYes =
      tailoringCyaIsAnswered && userAnswers
        .get(ProfessionalServiceExpensesPage, Some(businessId))
        .contains(ProfessionalServiceExpenses.ProfessionalFees)
    val interestIsYes =
      tailoringCyaIsAnswered && userAnswers
        .get(FinancialExpensesPage, Some(businessId))
        .contains(FinancialExpenses.Interest)
    val depreciationIsYes = tailoringCyaIsAnswered && userAnswers.get(DepreciationPage, Some(businessId)).exists(_ != Depreciation.No)

    Seq(
      buildRow(Abroad, abroadStatus),
      buildRow(Income, findJourneyStatus(journeyCompletedStates, Income, abroadStatus != Completed)),
      buildOptionalRow(ExpensesTailoring, findJourneyStatus(journeyCompletedStates, ExpensesTailoring, incomeStatus != Completed), declareExpenses),
      buildOptionalRow(ExpensesOfficeSupplies, findJourneyStatus(journeyCompletedStates, ExpensesOfficeSupplies), officeSuppliesIsYes),
      buildOptionalRow(ExpensesGoodsToSellOrUse, findJourneyStatus(journeyCompletedStates, ExpensesGoodsToSellOrUse), goodsToSellOrUseIsYes),
      buildOptionalRow(
        ExpensesRepairsAndMaintenance,
        findJourneyStatus(journeyCompletedStates, ExpensesRepairsAndMaintenance),
        repairsAndMaintenanceIsYes),
      buildOptionalRow(ExpensesAdvertisingOrMarketing, findJourneyStatus(journeyCompletedStates, ExpensesAdvertisingOrMarketing), advertisingIsYes),
      buildOptionalRow(ExpensesEntertainment, findJourneyStatus(journeyCompletedStates, ExpensesEntertainment), entertainmentsIsYes),
      buildOptionalRow(ExpensesStaffCosts, findJourneyStatus(journeyCompletedStates, ExpensesStaffCosts), staffCostsIsYes),
      buildOptionalRow(ExpensesConstruction, findJourneyStatus(journeyCompletedStates, ExpensesConstruction), constructionIsYes),
      buildOptionalRow(ExpensesProfessionalFees, findJourneyStatus(journeyCompletedStates, ExpensesProfessionalFees), professionalFeesIsYes),
      buildOptionalRow(ExpensesInterest, findJourneyStatus(journeyCompletedStates, ExpensesInterest), interestIsYes),
      buildOptionalRow(ExpensesDepreciation, findJourneyStatus(journeyCompletedStates, ExpensesDepreciation), depreciationIsYes)
    ).flatten
  }

  // noinspection ScalaStyle
  private def buildRow(journey: Journey, status: JourneyStatus): Option[String] = {
    val href = status match {
      case CannotStartYet               => "#"
      case NotStarted | CheckOurRecords => chooseFirstUrl(journey)
      case InProgress | Completed       => chooseCyaUrl(journey)
    }

    val optDeadlink = if (status == CannotStartYet) " class='govuk-deadlink'" else ""
    Some(
      s"SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlink> journeys.$journey </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem($href,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> status.$status </strong>),None, tag-float,Map())))))")
  }

  // noinspection ScalaStyle
  private def buildOptionalRow(journey: Journey, status: JourneyStatus, conditionPassedToShowOnTaskList: Boolean): Option[String] =
    if (conditionPassedToShowOnTaskList) {
      val href = status match {
        case NotStarted | CheckOurRecords => chooseFirstUrl(journey)
        case _                            => chooseCyaUrl(journey)
      }
      Some(
        s"SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href> journeys.$journey </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem($href,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> status.$status </strong>),None, tag-float,Map())))))")
    } else {
      None
    }

  // noinspection ScalaStyle
  private def chooseFirstUrl(journey: Journey): String =
    journey match {
      case Abroad            => journeys.abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
      case Income            => journeys.income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesTailoring => journeys.expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesOfficeSupplies =>
        journeys.expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesGoodsToSellOrUse =>
        journeys.expenses.goodsToSellOrUse.routes.GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesRepairsAndMaintenance =>
        journeys.expenses.repairsandmaintenance.routes.RepairsAndMaintenanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesAdvertisingOrMarketing =>
        journeys.expenses.advertisingOrMarketing.routes.AdvertisingAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesEntertainment =>
        journeys.expenses.entertainment.routes.EntertainmentAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesStaffCosts =>
        journeys.expenses.staffCosts.routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesConstruction =>
        journeys.expenses.construction.routes.ConstructionIndustryAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesProfessionalFees =>
        journeys.expenses.professionalFees.routes.ProfessionalFeesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesInterest =>
        journeys.expenses.interest.routes.InterestAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesDepreciation =>
        journeys.expenses.depreciation.routes.DepreciationDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case _ => "not implemented or error"
    }
  // noinspection ScalaStyle
  private def chooseCyaUrl(journey: Journey): String =
    journey match {
      case Abroad                   => journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
      case Income                   => journeys.income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesTailoring        => journeys.expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesOfficeSupplies   => journeys.expenses.officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesGoodsToSellOrUse => journeys.expenses.goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesRepairsAndMaintenance =>
        journeys.expenses.repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesAdvertisingOrMarketing =>
        journeys.expenses.advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesEntertainment    => journeys.expenses.entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesStaffCosts       => journeys.expenses.staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesConstruction     => journeys.expenses.construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesProfessionalFees => journeys.expenses.professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesInterest         => journeys.expenses.interest.routes.InterestCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesDepreciation =>
        journeys.expenses.depreciation.routes.DepreciationCYAController.onPageLoad(taxYear, businessId).url
      case _ => "not implemented or error"
    }

  private def findJourneyStatus(journeyCompletedStates: List[JourneyNameAndStatus],
                                journey: Journey,
                                cannotStartYet: Boolean = false): JourneyStatus = {
    val jcs = journeyCompletedStates
      .find(_.name == journey)
      .getOrElse(JourneyNameAndStatus(journey, JourneyStatus.NotStarted))

    jcs.journeyStatus match {
      case NotStarted => if (cannotStartYet) CannotStartYet else NotStarted
      case state      => state
    }
  }
}
