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

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.structuresBuildingsAllowance.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding.newStructure
import models.requests.DataRequest
import pages.OneQuestionPage
import play.api.data.Form
import play.api.mvc.{Call, Result}

trait StructuresBuildingsBasePage[A] extends OneQuestionPage[A] {
  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.StructuresBuildingsCYAController.onPageLoad(taxYear, businessId)

  def getStructureFromIndex(userAnswers: UserAnswers, businessId: BusinessId, index: Int): Option[NewStructureBuilding] =
    userAnswers.get(NewStructuresBuildingsList, businessId.some).map(list => if (list.length > index) list(index) else newStructure)

  def fillFormWithIndex[B](form: Form[B], page: StructuresBuildingsBasePage[B], request: DataRequest[_], businessId: BusinessId, index: Int): Form[B] = {
    val existingStructure: Option[NewStructureBuilding] = getStructureFromIndex(request.userAnswers, businessId, index)
    val existingValue: Option[B] = page match {
      case StructuresBuildingsQualifyingUseDatePage => existingStructure.flatMap(_.qualifyingUse)
      case StructuresBuildingsLocationPage => existingStructure.flatMap(_.newStructureBuildingLocation)
      case StructuresBuildingsNewClaimAmountPage => existingStructure.flatMap(_.newStructureBuildingClaimingAmount)
      case _ => None
    }
    existingValue.fold(form)(form.fill)
  }
  def nextPageWithIndex(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear, index: Int): Result = ???


}
