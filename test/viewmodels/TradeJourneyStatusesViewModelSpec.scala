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
import models.journeys.Journey
import models.journeys.Journey.{
  Abroad,
  ExpensesEntertainment,
  ExpensesGoodsToSellOrUse,
  ExpensesOfficeSupplies,
  ExpensesRepairsAndMaintenance,
  ExpensesTailoring,
  Income
}
import models.journeys.expenses.individualCategories.{EntertainmentCosts, GoodsToSellOrUse, OfficeSupplies, RepairsAndMaintenance}
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyCompletedState
import pages.expenses.tailoring.individualCategories.{EntertainmentCostsPage, GoodsToSellOrUsePage, OfficeSuppliesPage, RepairsAndMaintenancePage}
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

class TradeJourneyStatusesViewModelSpec extends SpecBase {

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  case class TestScenario(journeyCompletedStates: List[JourneyCompletedState], userAnswers: UserAnswers)
  private val officeSuppliesAndGTSOUYes = emptyUserAnswers
    .set(OfficeSuppliesPage, OfficeSupplies.YesAllowable, Some(businessId))
    .success
    .value
    .set(GoodsToSellOrUsePage, GoodsToSellOrUse.YesDisallowable, Some(businessId))
    .success
    .value
  private val testScenarios = Seq(
    TestScenario(List.empty, emptyUserAnswers),
    TestScenario(List(JourneyCompletedState(Abroad, Some(false))), emptyUserAnswers),
    TestScenario(
      List(JourneyCompletedState(Abroad, Some(true)), JourneyCompletedState(Income, None), JourneyCompletedState(ExpensesTailoring, Some(true))),
      emptyUserAnswers
    ),
    TestScenario(
      List(
        JourneyCompletedState(Abroad, Some(true)),
        JourneyCompletedState(Income, Some(true)),
        JourneyCompletedState(ExpensesTailoring, Some(true)),
        JourneyCompletedState(ExpensesOfficeSupplies, None),
        JourneyCompletedState(ExpensesGoodsToSellOrUse, Some(false))
      ),
      officeSuppliesAndGTSOUYes
    )
  ) // TODO find a better solution for generating combinations of different journey states and user answers

  "TradeJourneyStatusesViewModel" - {
    ".buildSummaryList" - {
      "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" - {
        testScenarios.foreach { testScenario =>
          s"statuses are: ${testScenario.journeyCompletedStates} and userAnswers are: ${testScenario.userAnswers}" in {
            val tradesJourneyStatuses = TradesJourneyStatuses(businessId.value, Some("tradingName"), testScenario.journeyCompletedStates)
            val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear, Some(testScenario.userAnswers))

            result.rows.map(_.toString) mustEqual buildExpectedResult(testScenario.journeyCompletedStates, testScenario.userAnswers)
          }
        }
      }
    }
  }

  private def buildExpectedResult(journeyCompletedStates: List[JourneyCompletedState], userAnswers: UserAnswers): Seq[String] = {
    val abroadStatus               = findJourneyStatus(journeyCompletedStates, Abroad)
    val officeSuppliesIsYes        = userAnswers.get(OfficeSuppliesPage, Some(businessId)).exists(_ != OfficeSupplies.No)
    val goodsToSellOrUseIsYes      = userAnswers.get(GoodsToSellOrUsePage, Some(businessId)).exists(_ != GoodsToSellOrUse.No)
    val entertainmentsIsYes        = userAnswers.get(EntertainmentCostsPage, Some(businessId)).exists(_ != EntertainmentCosts.No)
    val repairsAndMaintenanceIsYes = userAnswers.get(RepairsAndMaintenancePage, Some(businessId)).exists(_ != RepairsAndMaintenance.No)
    Seq(
      buildRow(Abroad, abroadStatus),
      buildRow(Income, findJourneyStatus(journeyCompletedStates, Income, abroadStatus != Completed)),
      buildRow(ExpensesTailoring, findJourneyStatus(journeyCompletedStates, ExpensesTailoring, abroadStatus != Completed)),
      buildOptionalRow(ExpensesOfficeSupplies, findJourneyStatus(journeyCompletedStates, ExpensesOfficeSupplies), officeSuppliesIsYes),
      buildOptionalRow(ExpensesGoodsToSellOrUse, findJourneyStatus(journeyCompletedStates, ExpensesGoodsToSellOrUse), goodsToSellOrUseIsYes),
      buildOptionalRow(ExpensesEntertainment, findJourneyStatus(journeyCompletedStates, ExpensesEntertainment), entertainmentsIsYes),
      buildOptionalRow(
        ExpensesRepairsAndMaintenance,
        findJourneyStatus(journeyCompletedStates, ExpensesRepairsAndMaintenance),
        repairsAndMaintenanceIsYes)
    ).filterNot(_ == "")
  }

  // noinspection ScalaStyle
  private def buildRow(journey: Journey, status: JourneyStatus): String = {
    val href = status match {
      case CannotStartYet               => "#"
      case NotStarted | CheckOurRecords => chooseFirstUrl(journey)
      case InProgress | Completed       => chooseCyaUrl(journey)
    }

    val optDeadlink = if (status == CannotStartYet) " class='govuk-deadlink'" else ""
    s"SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlink> journeys.$journey </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem($href,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> status.$status </strong>),None, tag-float,Map())))))"
  }

  // noinspection ScalaStyle
  private def buildOptionalRow(journey: Journey, status: JourneyStatus, conditionPassed: Boolean): String =
    if (conditionPassed) {
      val href = status match {
        case NotStarted => chooseFirstUrl(journey)
        case _          => chooseCyaUrl(journey)
      }
      s"SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href> journeys.$journey </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem($href,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> status.$status </strong>),None, tag-float,Map())))))"
    } else {
      ""
    }

  private def chooseFirstUrl(journey: Journey): String =
    journey match {
      case Abroad            => journeys.abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
      case Income            => journeys.income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesTailoring => journeys.expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesOfficeSupplies =>
        journeys.expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesGoodsToSellOrUse =>
        journeys.expenses.goodsToSellOrUse.routes.GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesEntertainment =>
        journeys.expenses.entertainment.routes.EntertainmentAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesRepairsAndMaintenance =>
        journeys.expenses.repairsandmaintenance.routes.RepairsAndMaintenanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case _ => "not implemented or error"
    }
  private def chooseCyaUrl(journey: Journey): String =
    journey match {
      case Abroad                   => journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
      case Income                   => journeys.income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesTailoring        => journeys.expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesOfficeSupplies   => journeys.expenses.officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesGoodsToSellOrUse => journeys.expenses.goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesEntertainment    => journeys.expenses.entertainment.routes.EntertainmentCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesRepairsAndMaintenance =>
        journeys.expenses.repairsandmaintenance.routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url
      case _ => "not implemented or error"
    }

  private def findJourneyStatus(journeyCompletedStates: List[JourneyCompletedState],
                                journey: Journey,
                                cannotStartYet: Boolean = false): JourneyStatus = {
    val jcs = journeyCompletedStates.find(_.journey == journey).getOrElse(JourneyCompletedState(journey, None))
    statusFromCompletedState(jcs.completedState) match {
      case NotStarted => if (cannotStartYet) CannotStartYet else NotStarted
      case state      => state
    }
  }
}
