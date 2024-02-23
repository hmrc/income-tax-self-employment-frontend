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
import models.journeys.Journey.{Abroad, CapitalAllowancesTailoring, CapitalAllowancesZeroEmissionCars, CapitalAllowancesZeroEmissionGoodsVehicle}
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.requests.TradesJourneyStatuses
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.journeys._
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryRow

object CapitalAllowancesTasklist {

  def buildCapitalAllowances(tradesJourneyStatuses: TradesJourneyStatuses, taxYear: TaxYear)(implicit
      messages: Messages,
      businessId: BusinessId,
      userAnswers: Option[UserAnswers]): List[SummaryListRow] = {

    val abroadIsCompleted = tradesJourneyStatuses.getStatusOrNotStarted(Abroad) == Completed
    val tailoringStatus   = getJourneyStatus(CapitalAllowancesTailoring, abroadIsCompleted)(tradesJourneyStatuses)
    val tailoringHref     = getCapitalAllowanceUrl(CapitalAllowancesTailoring, tailoringStatus, businessId, taxYear)
    val isCashHeading: String = userAnswers.map(_.getAccountingType(businessId)) match {
      case Some(Cash) => s".$Cash"
      case _          => ""
    }
    val tailoringRow = buildSummaryRow(tailoringHref, messages(s"journeys.$CapitalAllowancesTailoring$isCashHeading"), tailoringStatus).some

    val capAllowancesTailoringCompleted = tailoringStatus.isCompleted

    val zeroEmissionCarsStatus = getJourneyStatus(CapitalAllowancesZeroEmissionCars)(tradesJourneyStatuses)
    val zeroEmissionCarsHref   = getCapitalAllowanceUrl(CapitalAllowancesZeroEmissionCars, zeroEmissionCarsStatus, businessId, taxYear)
    val zecIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionCar) && capAllowancesTailoringCompleted
    val zeroEmissionCarsRow = returnRowIfConditionPassed(
      buildSummaryRow(zeroEmissionCarsHref, messages(s"journeys.$CapitalAllowancesZeroEmissionCars"), zeroEmissionCarsStatus),
      zecIsTailored
    )

    val zeroEmissionGoodsVehicleStatus = getJourneyStatus(CapitalAllowancesZeroEmissionGoodsVehicle)(tradesJourneyStatuses)
    val zeroEmissionGoodsVehicleHref =
      getCapitalAllowanceUrl(CapitalAllowancesZeroEmissionGoodsVehicle, zeroEmissionGoodsVehicleStatus, businessId, taxYear)
    val zegvIsTailored =
      conditionPassedForViewableLink(SelectCapitalAllowancesPage, CapitalAllowances.ZeroEmissionGoodsVehicle) && capAllowancesTailoringCompleted
    val zeroEmissionGoodsVehicleRow = returnRowIfConditionPassed(
      buildSummaryRow(zeroEmissionGoodsVehicleHref, messages(s"journeys.$CapitalAllowancesZeroEmissionGoodsVehicle"), zeroEmissionGoodsVehicleStatus),
      zegvIsTailored
    )

    List(tailoringRow, zeroEmissionCarsRow, zeroEmissionGoodsVehicleRow).flatten
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
      case CapitalAllowancesZeroEmissionGoodsVehicle =>
        determineJourneyStartOrCyaUrl(
          capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleController.onPageLoad(taxYear, businessId, NormalMode).url,
          capitalallowances.zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId).url
        )(journeyStatus)
      case _ => ???
    }
}
