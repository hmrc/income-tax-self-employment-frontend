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

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.nics
import models.NormalMode
import models.common.{JourneyStatus, TaxYear}
import models.journeys.Journey.{NationalInsuranceContributions, ProfitOrLoss}
import models.journeys.JourneyNameAndStatus
import models.journeys.nics.TaxableProfitAndLoss
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.TaxYearUtils.{currentTaxYearStartDate, dateNow}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{SummaryListCYA, determineJourneyStartOrCyaUrl, getJourneyStatus}

import java.time.{LocalDate, Period}

object NationalInsuranceContributionsViewModel {

  private val class2Href = (taxYear: TaxYear) => nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url
  private val class4Href = (taxYear: TaxYear) => nics.routes.Class4NICsController.onPageLoad(taxYear, NormalMode).url

  val statePensionAge = 66 // TODO this needs to be a config value

  // TODO does this need to check for different taxYears?
  private def ageBetween16AndStatePension(userDoB: LocalDate, ageAtStartOfTaxYear: Boolean): Boolean = {
    val comparisonDate = if (ageAtStartOfTaxYear) currentTaxYearStartDate else dateNow
    val age            = Period.between(userDoB, comparisonDate).getYears
    16 <= age && age < statePensionAge
  }

  def areAdjustmentsAnswered(tradeStatuses: List[TradesJourneyStatuses]): Boolean =
    tradeStatuses.nonEmpty && tradeStatuses.forall(s => JourneyStatus.getJourneyStatus(ProfitOrLoss, s.journeyStatuses).isCompleted)

  def checkClass2(taxableProfitsAndLosses: List[TaxableProfitAndLoss], userDoB: LocalDate): Boolean = {
    val profitsUnderThreshold = taxableProfitsAndLosses.map(_.taxableProfit).sum < 6725
    val hasAnyLosses          = taxableProfitsAndLosses.map(_.taxableLoss).sum != 0
    val ageIsValid            = ageBetween16AndStatePension(userDoB, ageAtStartOfTaxYear = false)
    ageIsValid && (profitsUnderThreshold || hasAnyLosses)
  }

  def checkClass4(taxableProfitsAndLosses: List[TaxableProfitAndLoss], userDoB: LocalDate): Boolean = {
    val profitsOverThreshold = taxableProfitsAndLosses.map(_.taxableProfit).sum > 12570
    val ageIsValid           = ageBetween16AndStatePension(userDoB, ageAtStartOfTaxYear = true)
    ageIsValid && profitsOverThreshold
  }

  def buildSummaryList(nationalInsuranceStatuses: Option[JourneyNameAndStatus],
                       tradeStatuses: List[TradesJourneyStatuses],
                       userDoB: LocalDate,
                       taxableProfitsAndLosses: List[TaxableProfitAndLoss],
                       taxYear: TaxYear)(implicit messages: Messages): SummaryList = {
    val linkIsClickable     = areAdjustmentsAnswered(tradeStatuses)
    val maybeSavedNicStatus = nationalInsuranceStatuses.fold(List.empty[JourneyNameAndStatus])(List(_))
    val updatedStatus       = getJourneyStatus(NationalInsuranceContributions, linkIsClickable)(maybeSavedNicStatus)

    val class2Eligible = checkClass2(taxableProfitsAndLosses, userDoB)
    val class4Eligible = checkClass4(taxableProfitsAndLosses, userDoB)
    val userIsEligible = class2Eligible || class4Eligible

    SummaryListCYA.summaryListOpt(
      List(
        if (userIsEligible) {
          val firstPageHref = if (class4Eligible) class4Href(taxYear) else class2Href(taxYear)
          val cyaHref       = nics.routes.NICsCYAController.onPageLoad(taxYear).url
          val correctHref   = determineJourneyStartOrCyaUrl(firstPageHref, cyaHref)(updatedStatus)

          buildSummaryRow(correctHref, messages(s"journeys.$NationalInsuranceContributions"), updatedStatus).some
        } else {
          None
        }
      ))
  }

}
