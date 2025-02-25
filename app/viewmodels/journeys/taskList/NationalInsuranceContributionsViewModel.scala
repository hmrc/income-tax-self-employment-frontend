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
import models.common.Journey.{NationalInsuranceContributions, ProfitOrLoss}
import models.common.{JourneyStatus, TaxYear}
import models.journeys.JourneyNameAndStatus
import models.journeys.nics.NicClassExemption.{Class2, Class4, NotEligible}
import models.journeys.nics.TaxableProfitAndLoss
import models.journeys.nics.TaxableProfitAndLoss.returnClassTwoOrFourEligible
import models.requests.TradesJourneyStatuses
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow
import viewmodels.journeys.{SummaryListCYA, determineJourneyStartOrCyaUrl, getJourneyStatus}

import java.time.LocalDate

object NationalInsuranceContributionsViewModel {

  private val class2Href = (taxYear: TaxYear) => nics.routes.Class2NICsController.onPageLoad(taxYear, NormalMode).url
  private val class4Href = (taxYear: TaxYear) => nics.routes.Class4NICsController.onPageLoad(taxYear, NormalMode).url
  private val cyaHref    = (taxYear: TaxYear) => nics.routes.NICsCYAController.onPageLoad(taxYear).url

  def areAdjustmentsAnswered(tradeStatuses: List[TradesJourneyStatuses]): Boolean =
    tradeStatuses.nonEmpty && tradeStatuses.forall(s => JourneyStatus.getJourneyStatus(ProfitOrLoss, s.journeyStatuses).isCompleted)

  def buildSummaryList(nationalInsuranceStatuses: Option[JourneyNameAndStatus],
                       tradeStatuses: List[TradesJourneyStatuses],
                       userDoB: Option[LocalDate],
                       taxableProfitsAndLosses: List[TaxableProfitAndLoss],
                       taxYear: TaxYear)(implicit messages: Messages): SummaryList = {
    val linkIsClickable     = areAdjustmentsAnswered(tradeStatuses)
    val maybeSavedNicStatus = nationalInsuranceStatuses.fold(List.empty[JourneyNameAndStatus])(List(_))
    val updatedStatus       = getJourneyStatus(NationalInsuranceContributions, linkIsClickable)(maybeSavedNicStatus)

    val classTwoOrFourEligibility = returnClassTwoOrFourEligible(taxableProfitsAndLosses, userDoB, taxYear)

    def buildRow(firstPageHref: String): Option[SummaryListRow] = {
      val getCorrectHref = determineJourneyStartOrCyaUrl(firstPageHref, cyaHref(taxYear))(updatedStatus)
      buildSummaryRow(getCorrectHref, messages(s"journeys.$NationalInsuranceContributions"), updatedStatus).some
    }

    SummaryListCYA.summaryListOpt(List(classTwoOrFourEligibility match {
      case Class4      => buildRow(class4Href(taxYear))
      case Class2      => buildRow(class2Href(taxYear))
      case NotEligible => None
    }))
  }

}
