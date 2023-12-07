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
import models.journeys.Journey
import models.journeys.Journey.{Abroad, ExpensesOfficeSupplies, ExpensesTailoring, Income}
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyCompletedState
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

import scala.annotation.nowarn

class TradeJourneyStatusesViewModelSpec extends SpecBase {

  case class TestScenario(journeyCompletedStates: List[JourneyCompletedState])

  private val testScenarios = Seq(
    TestScenario(List.empty),
    TestScenario(List(JourneyCompletedState(Abroad, Some(false))))
  )// First 3 should always appear, others should be conditional

  "TradeJourneyStatusesViewModel" - {
    ".buildSummaryList" - {
      testScenarios.foreach { testScenario =>
        "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" - {
          s"statuses are: ${testScenario.journeyCompletedStates}" in {
            val tradesJourneyStatuses = TradesJourneyStatuses(businessId.value, Some("tradingName"), testScenario.journeyCompletedStates)
            val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear, Some(emptyUserAnswers))

            result.rows.map(_.toString) mustEqual buildExpectedResult(testScenario.journeyCompletedStates)
          }
        }
      }
    }
  }

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  private def buildExpectedResult(journeyCompletedStates: List[JourneyCompletedState]): Seq[String] = {
    if (journeyCompletedStates.isEmpty) {
      Seq(buildRow(Abroad, statusFromCompletedState(None)))
    } else {
      journeyCompletedStates.map(jcs => buildRow(jcs.journey, statusFromCompletedState(jcs.completedState)))
    }
  }

  // noinspection ScalaStyle
  @nowarn("msg=match may not be exhaustive")
  private def buildRow(journey: Journey, status: JourneyStatus): String = {
    val href = status match {
      case CannotStartYet         => "#"
      case NotStarted             => chooseFirstUrl(journey)
      case InProgress | Completed => chooseCyaUrl(journey)
    }

    val optDeadlink = if (status == CannotStartYet) " class='govuk-deadlink'" else ""
    s"SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=$href$optDeadlink> journeys.$journey </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem($href,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--$status'> status.$status </strong>),None, tag-float,Map())))))"
  }

  private def chooseFirstUrl(journey: Journey): String =
    journey match {
      case Abroad            => journeys.abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
      case Income            => journeys.income.routes.IncomeNotCountedAsTurnoverController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesTailoring => journeys.expenses.tailoring.routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode).url
      case ExpensesOfficeSupplies =>
        journeys.expenses.officeSupplies.routes.OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
      case _ => "not implemented or error"
    }
  private def chooseCyaUrl(journey: Journey): String =
    journey match {
      case Abroad                 => journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url
      case Income                 => journeys.income.routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesTailoring      => journeys.expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId).url
      case ExpensesOfficeSupplies => journeys.expenses.officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url
      case _                      => "not implemented or error"
    }
}
