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
import models.NormalMode
import models.common._
import models.database.UserAnswers
import pages.redirectOnBoolean
import play.api.mvc.Call
import queries.Settable

object StructuresBuildingsRemovePage extends StructuresBuildingsBasePage[Boolean] {
  override def toString: String = "structuresBuildingsRemove"

  override val dependentPagesWhenNo: List[Settable[_]] =
    List(
      // TODO change page
    )

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    redirectOnBoolean(
      this,
      userAnswers,
      businessId,
      onTrue = routes.StructuresBuildingsRemoveController
        .onPageLoad(taxYear, businessId, NormalMode), // TODO change to new-structures summary page
      onFalse = cyaPage(taxYear, businessId) //TODO change to new-structures summary page unless no buildings left
    )

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean = {
    val answer = userAnswers.get(this, businessId)
    answer.contains(false)
  } // TODO change page
}
