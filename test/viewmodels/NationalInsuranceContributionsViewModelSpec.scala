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
import builders.BusinessDataBuilder.smallProfitTaxableProfitAndLoss
import builders.TradesJourneyStatusesBuilder.{aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel}
import controllers.journeys._
import models.NormalMode
import models.common.Journey._
import models.common.JourneyStatus._
import models.common.TaxYear.dateNow
import models.common._
import models.journeys.JourneyNameAndStatus
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.NationalInsuranceContributionsViewModelSpec.expectedRow
import viewmodels.journeys.taskList.NationalInsuranceContributionsViewModel
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

class NationalInsuranceContributionsViewModelSpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val canNotStartUrl = "#"
  private val nicUrl         = nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url
  private val nicCyaUrl      = nics.routes.NICsCYAController.onPageLoad(taxYear).url

  private val nicNotStartedStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.NotStarted))

  private val nicInProgressStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.InProgress))

  private val nicCompleteStatus: Option[JourneyNameAndStatus] = Some(JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.Completed))

  private val testScenarios = Table(
    ("nationalInsuranceStatus", "businessStatuses", "expected"),
    // if adjustments in businessStatuses are not ALL completed then the UI status will be CannotStartYet regardless of the saved NICStatus.
    (
      None,
      List(anEmptyTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.NotStarted)))),
      List(expectedRow(canNotStartUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicNotStartedStatus,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.NotStarted)))),
      List(expectedRow(canNotStartUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicInProgressStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.NotStarted))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName3")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.InProgress)))
      ),
      List(expectedRow(canNotStartUrl, NationalInsuranceContributions, CannotStartYet))),
    (
      nicCompleteStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(tradingName = Some(TradingName("TradingName3")), journeyStatuses = List.empty)
      ),
      List(expectedRow(canNotStartUrl, NationalInsuranceContributions, CannotStartYet))),

    // if adjustments in businessStatuses are ALL completed then the UI status will match the saved NICStatus or default to NotStarted.
    (
      None,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))),
      List(expectedRow(nicUrl, NationalInsuranceContributions, NotStarted))),
    (
      nicNotStartedStatus, // When backend returns NotStarted, answers have been submitted but Have You Completed page not answered -> status is InProgress
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))
      ),
      List(expectedRow(nicCyaUrl, NationalInsuranceContributions, InProgress))),
    (
      nicInProgressStatus,
      List(
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName1")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName2")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(
          tradingName = Some(TradingName("TradingName3")),
          journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))
      ),
      List(expectedRow(nicCyaUrl, NationalInsuranceContributions, InProgress))),
    (
      nicCompleteStatus,
      List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))),
      List(expectedRow(nicCyaUrl, NationalInsuranceContributions, Completed)))
  )

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (nationalInsuranceStatus, businessStatuses, expectedRows) =>
        val result = NationalInsuranceContributionsViewModel.buildSummaryList(
          nationalInsuranceStatus,
          businessStatuses,
          Some(dateNow.minusYears(20)),
          smallProfitTaxableProfitAndLoss,
          taxYear)(messages)

        withClue(s"""
             |Result:
             |${result.rows.mkString("\n")}
             |did not equal expected result:
             |${expectedRows.mkString("\n")}
             |""".stripMargin) {
          assert(result.rows === expectedRows)
        }
      }
    }
    "must return an empty summary list when user is ineligible for Class 2 or Class 4" in {
      val result =
        NationalInsuranceContributionsViewModel.buildSummaryList(
          None,
          List.empty,
          Some(dateNow.minusYears(15)),
          smallProfitTaxableProfitAndLoss,
          taxYear)(messages)
      val emptySummaryList = SummaryList(List.empty[SummaryListRow], None, "govuk-!-margin-bottom-7")

      assert(result === emptySummaryList)
    }
  }

  private val adjustmentTestScenarios = Table(
    ("businessStatuses", "expectedResult"),
    (List.empty, false),
    (List(anEmptyTadesJourneyStatusesModel), false),
    (List(aTadesJourneyStatusesModel.copy(journeyStatuses = List.empty)), false),
    (List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.CannotStartYet)))), false),
    (List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.NotStarted)))), false),
    (List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.InProgress)))), false),
    (
      List(
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(journeyStatuses = List.empty)
      ),
      false),
    (
      List(
        aTadesJourneyStatusesModel.copy(journeyStatuses = List.empty),
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))
      ),
      false),
    (
      List(
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.CannotStartYet))),
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))
      ),
      false),
    (List(aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))), true),
    (
      List(
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed))),
        aTadesJourneyStatusesModel.copy(journeyStatuses = List(JourneyNameAndStatus(ProfitOrLoss, JourneyStatus.Completed)))
      ),
      true)
  )

  "areAdjustmentsAnswered" - {
    "should return true when there are Adjustment journey statuses saved, that are all 'Completed'" in {
      forAll(adjustmentTestScenarios) { case (businessStatuses, expectedResult) =>
        val result = NationalInsuranceContributionsViewModel.areAdjustmentsAnswered(businessStatuses)

        withClue(s"""
                    |Result:
                    |$result
                    |did not equal expected result:
                    |$expectedResult
                    |""".stripMargin) {
          assert(result === expectedResult)
        }
      }
    }
  }
}

object NationalInsuranceContributionsViewModelSpec {

  def expectedRow(href: String, journey: Journey, status: JourneyStatus)(implicit messages: Messages): SummaryListRow =
    buildSummaryRow(href, s"journeys.$journey", status)

}
