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
import controllers.journeys._
import models.NormalMode
import models.common.JourneyStatus
import models.common.JourneyStatus._
import models.journeys.Journey
import models.journeys.Journey._
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.NationalInsuranceContributionsViewModelSpec.expectedRow
import viewmodels.journeys.taskList.NationalInsuranceContributionsViewModel
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

class NationalInsuranceContributionsViewModelSpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val nicUrl = nics.routes.Class2NICsController.onPageLoad(taxYear, businessId, NormalMode).url

  private val testScenarios = Table(
    ("nationalInsuranceStatuses", "userAnswers", "expected"),
    // No statuses, no answers, defaults to cannot start yet until the saving is implemented
    (Nil, Nil, List(expectedRow(nicUrl, NationalInsuranceContributions, CannotStartYet)))
  )

  "buildSummaryList" - {
    "must create a SummaryList with the correct amount of rows, URLs and journey statuses when" in {
      forAll(testScenarios) { case (nationalInsuranceStatuses, _, expectedRows) =>
        val result = NationalInsuranceContributionsViewModel.buildSummaryList(nationalInsuranceStatuses)(messages, taxYear, businessId)

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
