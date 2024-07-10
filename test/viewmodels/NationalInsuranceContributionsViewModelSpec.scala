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

package viewmodels

import base.SpecBase
import builders.TradesJourneyStatusesBuilder.{aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel}
import controllers.journeys._
import models.NormalMode
import models.common.JourneyStatus._
import models.common.{JourneyStatus, TradingName}
import models.journeys.Journey._
import models.journeys.{Journey, JourneyNameAndStatus}
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.NationalInsuranceContributionsViewModelSpec.expectedRow
import viewmodels.journeys.taskList.NationalInsuranceContributionsViewModel
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

class NationalInsuranceContributionsViewModelSpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val nicUrl = nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url

  private val nicCannotStartStatus: Option[JourneyNameAndStatus] = Some(
    JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.CannotStartYet))

  private val nicNotStartedStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.NotStarted))

  private val nicInProgressStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.InProgress))

  private val nicCompleteStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.Completed))

  private val testScenarios = Table(
    ("nationalInsuranceStatus", "businessStatuses", "expected"),
    // If Business status does not contain completed adjustment status; NIC status should be CanNotStartYet
    (
      None,
      List(anEmptyTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.CannotStartYet)))),
      List(expectedRow(nicUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicCannotStartStatus,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.NotStarted)))),
      List(expectedRow(nicUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicCannotStartStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.InProgress))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.NotStarted)))
      ),
      List(expectedRow(nicUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicCannotStartStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.CannotStartYet))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.NotStarted))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName3")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.InProgress)))
      ),
      List(expectedRow(nicUrl, NationalInsuranceContributions, CannotStartYet))),

    // If Adjustments is completed, it should show NIC status as NotStarted
    (
      None,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed)))),
      List(expectedRow(nicUrl, NationalInsuranceContributions, NotStarted))),
    (
      nicNotStartedStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed)))
      ),
      List(expectedRow(nicUrl, NationalInsuranceContributions, NotStarted))),
    (
      nicInProgressStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName3")),
          journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed)))
      ),
      List(expectedRow(nicUrl, NationalInsuranceContributions, InProgress))),
    (
      nicCompleteStatus,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(Adjustments, JourneyStatus.Completed)))),
      List(expectedRow(nicUrl, NationalInsuranceContributions, Completed)))
  )

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (nationalInsuranceStatus, businessStatuses, expectedRows) =>
        val result = NationalInsuranceContributionsViewModel.buildSummaryList(nationalInsuranceStatus, businessStatuses, taxYear)(messages)

        withClue(s"""
             |${result.rows.mkString("\n")}
             |did not equal:
             |${expectedRows.mkString("\n")}
             |""".stripMargin) {
          assert(result.rows === expectedRows)
        }
      }
    }
  }

}

object NationalInsuranceContributionsViewModelSpec {

  def expectedRow(href: String, journey: Journey, status: JourneyStatus)(implicit messages: Messages): SummaryListRow =
    buildSummaryRow(href, s"journeys.$journey", status)

}
