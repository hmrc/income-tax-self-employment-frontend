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

package models.journeys.capitalallowances.structuresBuildingsAllowance

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class NewStructureBuilding(qualifyingUse: Option[LocalDate] = None,
                                newStructureBuildingQualifyingExpenditureAmount: Option[BigDecimal] = None,
                                newStructureBuildingLocation: Option[StructuresBuildingsLocation] = None,
                                newStructureBuildingClaimingAmount: Option[BigDecimal] = None) {
  def isComplete: Boolean =
    qualifyingUse.isDefined &&
      newStructureBuildingQualifyingExpenditureAmount.isDefined &&
      newStructureBuildingLocation.isDefined &&
      newStructureBuildingClaimingAmount.isDefined

  def isEmpty: Boolean = Seq(
    qualifyingUse,
    newStructureBuildingLocation,
    newStructureBuildingClaimingAmount,
    newStructureBuildingQualifyingExpenditureAmount
  ).forall(_.isEmpty)

}

object NewStructureBuilding {
  implicit val formats: Format[NewStructureBuilding] = Json.format[NewStructureBuilding]

  def newStructure: NewStructureBuilding = NewStructureBuilding()
}
