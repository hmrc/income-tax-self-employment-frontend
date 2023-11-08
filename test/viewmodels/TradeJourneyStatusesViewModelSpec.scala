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
import models.journeys.{Abroad, Income}
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyStatus
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

class TradeJourneyStatusesViewModelSpec extends SpecBase {

  "TradeJourneyStatusesViewModelSpec" - {
    ".buildSummaryList" - {
      "must create a SummaryList with two rows for Abroad and Income with correct URLs and journey statuses when" - {
        "Abroad is NotStarted, and Income is CannotStart" in {
          val tradesJourneyStatuses = TradesJourneyStatuses(stubbedBusinessId, Some(tradingName), Seq.empty)
          val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear)

          result.rows.map(_.toString) mustEqual Seq(notStartedAbroadRow, cannotStartYetIncomeRow)
        }
        "Abroad is completed, and Income is NotStarted" in {
          val tradesJourneyStatuses = TradesJourneyStatuses(stubbedBusinessId, Some(tradingName), Seq(JourneyStatus(Abroad, Some(true))))
          val result                = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear)

          result.rows.map(_.toString) mustEqual Seq(completedAbroadRow, notStartedIncomeRow)
        }
        "Abroad and Income are both Completed" in {
          val tradesJourneyStatuses =
            TradesJourneyStatuses(stubbedBusinessId, Some(tradingName), Seq(JourneyStatus(Abroad, Some(true)), JourneyStatus(Income, Some(true))))
          val result = TradeJourneyStatusesViewModel.buildSummaryList(tradesJourneyStatuses, taxYear)

          result.rows.map(_.toString) mustEqual Seq(completedAbroadRow, completedIncomeRow)
        }
      }
    }
  }

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  private val tradingName = "tradingName"

  private val notStartedAbroadRow =
    "SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=/update-and-submit-income-tax-return/self-employment/2023/details/self-employment-abroad/SJPR05893938418> common.selfEmploymentAbroad </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem(/update-and-submit-income-tax-return/self-employment/2023/details/self-employment-abroad/SJPR05893938418,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--notStarted'> status.notStarted </strong>),None, tag-float,Map())))))"

  private val completedAbroadRow =
    "SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=/update-and-submit-income-tax-return/self-employment/2023/self-employment/details/check/SJPR05893938418> common.selfEmploymentAbroad </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem(/update-and-submit-income-tax-return/self-employment/2023/self-employment/details/check/SJPR05893938418,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--completed'> status.completed </strong>),None, tag-float,Map())))))"

  private val cannotStartYetIncomeRow =
    "SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=# class='govuk-deadlink'> common.income </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem(#,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--cannotStartYet'> status.cannotStartYet </strong>),None, tag-float,Map())))))"

  private val notStartedIncomeRow =
    "SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=/update-and-submit-income-tax-return/self-employment/2023/SJPR05893938418/income/not-counted-turnover> common.income </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem(/update-and-submit-income-tax-return/self-employment/2023/SJPR05893938418/income/not-counted-turnover,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--notStarted'> status.notStarted </strong>),None, tag-float,Map())))))"

  private val completedIncomeRow =
    "SummaryListRow(Key(HtmlContent(<span class='app-task-list__task-name govuk-!-font-weight-regular'> <a href=/update-and-submit-income-tax-return/self-employment/2023/SJPR05893938418/income/check-your-income> common.income </a> </span>),),Value(Empty,), app-task-list__item no-wrap no-after-content,Some(Actions(,List(ActionItem(/update-and-submit-income-tax-return/self-employment/2023/SJPR05893938418/income/check-your-income,HtmlContent(<strong class='govuk-tag app-task-list__tag govuk-tag--completed'> status.completed </strong>),None, tag-float,Map())))))"

}
