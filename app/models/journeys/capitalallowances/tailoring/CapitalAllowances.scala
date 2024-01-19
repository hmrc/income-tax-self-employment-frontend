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

package models.journeys.capitalallowances.tailoring

import models.common.{Enumerable, WithName}
import models.journeys.capitalallowances.tailoring.CapitalAllowances.{
  AnnualInvestment,
  Balancing,
  BalancingCharge,
  ElectricVehicleChargepoint,
  SpecialTaxSitesStructuresAndBuildings,
  StructuresAndBuildings,
  WritingDown,
  ZeroEmissionCar,
  ZeroEmissionGoodsVehicle
}

sealed trait CapitalAllowances

object CapitalAllowances extends Enumerable.Implicits {

  case object ZeroEmissionCar                       extends WithName("zeroEmissionCar") with CapitalAllowances
  case object ZeroEmissionGoodsVehicle              extends WithName("zeroEmissionGoodsVehicle") with CapitalAllowances
  case object ElectricVehicleChargepoint            extends WithName("electricVehicleChargepoint") with CapitalAllowances
  case object StructuresAndBuildings                extends WithName("structuresAndBuildings") with CapitalAllowances
  case object SpecialTaxSitesStructuresAndBuildings extends WithName("specialTaxSitesStructuresAndBuildings") with CapitalAllowances
  case object AnnualInvestment                      extends WithName("annualInvestment") with CapitalAllowances
  case object WritingDown                           extends WithName("writingDown") with CapitalAllowances
  case object Balancing                             extends WithName("balancing") with CapitalAllowances
  case object BalancingCharge                       extends WithName("balancingCharge") with CapitalAllowances

  val allAccrualAllowances: List[CapitalAllowances] =
    ZeroEmissionsGroup.accrualAllowances ++ StructuresAndBuildingsGroup.accrualAllowances ++ AssetAndAllowancesGroup.accrualAllowances

  val allCashAllowances: List[CapitalAllowances] =
    ZeroEmissionsGroup.cashAllowances ++ StructuresAndBuildingsGroup.cashAllowances ++ AssetAndAllowancesGroup.cashAllowances

  implicit val enumerable: Enumerable[CapitalAllowances] =
    Enumerable(
      (allCashAllowances ++ allAccrualAllowances.distinct)
        .map(v => v.toString -> v): _*)

}

sealed trait AllowanceType {
  val identifier: String
  val accrualAllowances: List[CapitalAllowances]
  val cashAllowances: List[CapitalAllowances]
}

object ZeroEmissionsGroup extends AllowanceType {
  override val identifier: String = "zeroEmissions"

  override val accrualAllowances: List[CapitalAllowances] =
    List(ZeroEmissionCar, ZeroEmissionGoodsVehicle, ElectricVehicleChargepoint)

  override val cashAllowances: List[CapitalAllowances] =
    List(ZeroEmissionCar)
}

object StructuresAndBuildingsGroup extends AllowanceType {
  override val identifier: String = "structuresAndBuildings"

  override val accrualAllowances: List[CapitalAllowances] =
    List(StructuresAndBuildings, SpecialTaxSitesStructuresAndBuildings)

  override val cashAllowances: List[CapitalAllowances] =
    List.empty
}

object AssetAndAllowancesGroup extends AllowanceType {
  override val identifier: String = "assetAndAllowances"

  override val accrualAllowances: List[CapitalAllowances] =
    List(AnnualInvestment, WritingDown, Balancing, BalancingCharge)

  override val cashAllowances: List[CapitalAllowances] =
    List(WritingDown, Balancing, BalancingCharge)
}
