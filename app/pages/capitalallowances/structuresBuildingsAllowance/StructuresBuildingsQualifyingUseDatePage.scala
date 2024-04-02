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

package pages.capitalallowances.structuresBuildingsAllowance

import controllers.journeys.capitalallowances.structuresBuildingsAllowance.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import java.time.LocalDate

object StructuresBuildingsQualifyingUseDatePage extends StructuresBuildingsBasePage[LocalDate] {
  override def toString: String = "structuresBuildingsQualifyingUseDate"

  def hasAllFurtherAnswers(structure: NewStructureBuilding): Boolean =
    structure.qualifyingUse.isDefined & StructuresBuildingsLocationPage.hasAllFurtherAnswers(structure)

  def nextPageWithIndex(mode: Mode, userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear, index: Int): Result =
    getStructureFromIndex(userAnswers, businessId, index) match {
      case None => redirectToRecoveryPage(s"Structure of index $index not found when redirecting from StructuresBuildingsQualifyingUseDatePage")
      case Some(structure) =>
        Redirect(mode match {
          case CheckMode if hasAllFurtherAnswers(structure) =>
            routes.StructuresBuildingsSummaryController.onPageLoad(taxYear, businessId, index)
          case NormalMode => routes.StructuresBuildingsLocationController.onPageLoad(taxYear, businessId, index, NormalMode)
          case _          => routes.StructuresBuildingsLocationController.onPageLoad(taxYear, businessId, index, NormalMode)
        })
    }
}
