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
import controllers.journeys.capitalallowances
import models.NormalMode
import models.common.AccountingType.Cash
import models.common.JourneyStatus.Completed
import models.common.{BusinessId, JourneyStatus, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.{Abroad, CapitalAllowancesTailoring, CapitalAllowancesZeroEmissionCars, CapitalAllowancesZeroEmissionGoodsVehicles}
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.requests.TradesJourneyStatuses
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehiclePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys._
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

object CapitalAllowancesTasklist {
  final case class TaskListBuidler(taxYear: TaxYear, tradesJourneyStatuses: TradesJourneyStatuses, tailoringStatus: JourneyStatus)(implicit
      messages: Messages,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers]
  ) {

    def getTailoringRow = {
      val isCashHeading: String = userAnswers.map(_.getAccountingType(businessId)) match {
        case Some(Cash) => s".$Cash"
        case _          => ""
      }
      val tailoringHref = getCapitalAllowanceUrl(CapitalAllowancesTailoring, tailoringStatus, businessId, taxYear)
      buildSummaryRow(tailoringHref, messages(s"journeys.$CapitalAllowancesTailoring$isCashHeading"), tailoringStatus).some
    }

    def getZeroEmissionCarsRow = {
      val capAllowancesTailoringCompleted = tailoringStatus.isCompleted
      val zeroEmissionCarsStatus          = getJourneyStatus(CapitalAllowancesZeroEmissionCars)(tradesJourneyStatuses)
      val zecIsTailored =
        conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionCar) && capAllowancesTailoringCompleted
      val zeroEmissionCarsHref = getCapitalAllowanceUrl(CapitalAllowancesZeroEmissionCars, zeroEmissionCarsStatus, businessId, taxYear)
      returnRowIfConditionPassed(
        buildSummaryRow(zeroEmissionCarsHref, messages(s"journeys.$CapitalAllowancesZeroEmissionCars"), zeroEmissionCarsStatus),
        zecIsTailored
      )
    }

    def getZeroEmissionGoodsVehiclesRow = {
      val capAllowancesTailoringCompleted = tailoringStatus.isCompleted
      val status                          = getJourneyStatus(CapitalAllowancesZeroEmissionGoodsVehicles)(tradesJourneyStatuses)
      val zecIsTailored =
        conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionGoodsVehicle) && capAllowancesTailoringCompleted
      val zeroEmissionCarsHref = getCapitalAllowanceUrl(CapitalAllowancesZeroEmissionGoodsVehicles, status, businessId, taxYear)
      returnRowIfConditionPassed(
        buildSummaryRow(zeroEmissionCarsHref, messages(s"journeys.$CapitalAllowancesZeroEmissionGoodsVehicles"), status),
        zecIsTailored
      )
    }

    private def getCapitalAllowanceUrl(journey: Journey, journeyStatus: JourneyStatus, businessId: BusinessId, taxYear: TaxYear): String =
      journey match {
        case CapitalAllowancesTailoring =>
          determineJourneyStartOrCyaUrl(
            capitalallowances.tailoring.routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode).url,
            capitalallowances.tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId).url
          )(journeyStatus)
        case CapitalAllowancesZeroEmissionCars =>
          determineJourneyStartOrCyaUrl(
            capitalallowances.zeroEmissionCars.routes.ZecUsedForWorkController.onPageLoad(taxYear, businessId, NormalMode).url,
            capitalallowances.zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId).url
          )(journeyStatus)
        case CapitalAllowancesZeroEmissionGoodsVehicles =>
          determineJourneyStartOrCyaUrl(
            capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleController.onPageLoad(taxYear, businessId, NormalMode).url,
            capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId).url
          )(journeyStatus)
        case _ => ???
      }

  }

  def buildCapitalAllowances(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear)(implicit
      messages: Messages,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers]): List[SummaryListRow] = {
    val isAbroadJourneyCompleted: Boolean = tradesJourneyStatuses.getStatusOrNotStarted(Abroad) == Completed
    val tailoringStatus: JourneyStatus    = getJourneyStatus(CapitalAllowancesTailoring, isAbroadJourneyCompleted)(tradesJourneyStatuses)
    val builder                           = TaskListBuidler(taxYear, tradesJourneyStatuses, tailoringStatus)

    List(
      builder.getTailoringRow,
      builder.getZeroEmissionCarsRow,
      builder.getZeroEmissionGoodsVehiclesRow
    ).flatten
  }

}
