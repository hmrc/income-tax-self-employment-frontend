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

package viewmodels.journeys.taskList

import controllers.journeys.nics
import models.NormalMode
import models.common.{JourneyStatus, TaxYear}
import models.journeys.Journey.{NationalInsuranceContributions, ProfitOrLoss}
import models.journeys.JourneyNameAndStatus
import models.journeys.nics.ExemptionCategory
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.TaxYearUtils.{currentTaxYearStartDate, dateNow}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{SummaryListCYA, determineJourneyStartOrCyaUrl, getJourneyStatus}

import java.time.{LocalDate, Period}

object NationalInsuranceContributionsViewModel {

  private val class2Href  = (taxYear: TaxYear) => nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url
  private val class4Href  = (taxYear: TaxYear) => nics.routes.Class4NICsController.onPageLoad(taxYear, NormalMode).url
  private val notEligible = "Not Eligible"

  val statePensionAge = 66 // TODO this needs to be a config value

  def areAdjustmentsAnswered(tradeStatuses: List[TradesJourneyStatuses]): Boolean =
    tradeStatuses.nonEmpty && tradeStatuses.forall(s => JourneyStatus.getJourneyStatus(ProfitOrLoss, s.journeyStatuses).isCompleted)

  def ageBetween16AndStatePension(dob: String, ageAtStartOfTaxYear: Boolean): Boolean = {
    val userDoB        = LocalDate.parse(dob)
    val comparisonDate = if (ageAtStartOfTaxYear) currentTaxYearStartDate else dateNow
    val age            = Period.between(userDoB, comparisonDate).getYears
    16 <= age && age < statePensionAge
  }

  def class2Eligible(taxableProfits: List[BigDecimal], taxableLosses: List[BigDecimal], ageIsValid: Boolean): Boolean = {
    val profitLossIsValid = taxableProfits.sum < 6725 || taxableLosses.nonEmpty
    // if they have a loss then they get class 2, even if they are over 6725 threshold
    profitLossIsValid && ageIsValid
  }
  def class4Eligible(taxableProfits: List[BigDecimal]): Boolean = taxableProfits.sum > 12570
  // citizen-details: If 16 =< age < state pension age QQQQ
  //  "taxableProfit" API 1415: taxable profit (or loss) SUM of businesses < 6725

  def buildSummaryList(nationalInsuranceStatuses: Option[JourneyNameAndStatus],
                       tradeStatuses: List[TradesJourneyStatuses],
                       taxableProfits: List[BigDecimal],
                       taxableLosses: List[BigDecimal],
                       userDoB: String,
                       taxYear: TaxYear)(implicit messages: Messages): SummaryList = {

    val class2: Boolean = class2Eligible(taxableProfits, taxableLosses, ageBetween16AndStatePension(userDoB, ageAtStartOfTaxYear = false))

    val class4: Boolean = class4Eligible(taxableProfits)

    val exmeptionReasons =
      if (ageBetween16AndStatePension(userDoB, ageAtStartOfTaxYear = false))
        ExemptionCategory.values    // If between, there may be more than two reasons in future
      else ExemptionCategory.values // If not between, show only these two reasons

    // Class 4 takes priority, then Class 2, what if neither?
    val firstPageHref = if (class4) class4Href(taxYear) else if (class2) class2Href(taxYear) else notEligible

    val linkIsClickable = areAdjustmentsAnswered(tradeStatuses)
    val journeyStatus =
      getJourneyStatus(NationalInsuranceContributions, linkIsClickable)(nationalInsuranceStatuses.fold(List.empty[JourneyNameAndStatus])(List(_)))

    val href = determineJourneyStartOrCyaUrl(firstPageHref, nics.routes.NICsCYAController.onPageLoad(taxYear).url)(journeyStatus)

    SummaryListCYA.summaryList(List(buildSummaryRow(href, messages(s"journeys.$NationalInsuranceContributions"), journeyStatus)))
  }
}
