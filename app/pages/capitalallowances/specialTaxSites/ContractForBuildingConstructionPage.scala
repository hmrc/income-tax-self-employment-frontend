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

package pages.capitalallowances.specialTaxSites

import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import queries.Settable

object ContractForBuildingConstructionPage extends SpecialTaxSitesBasePage[Boolean] {
  override def toString: String = "contractForBuildingConstruction"

  override val dependentPagesWhenYes: List[Settable[_]] = List(ConstructionStartDatePage)
  override val dependentPagesWhenNo: List[Settable[_]]  = List(ContractStartDatePage)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined &&
      (ConstructionStartDatePage.hasAllFurtherAnswers(businessId, userAnswers) ||
        ContractStartDatePage.hasAllFurtherAnswers(businessId, userAnswers))

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = ???
  def redirectNextWithIndex(answer: Boolean,
                            originalMode: Mode,
                            userAnswers: UserAnswers,
                            businessId: BusinessId,
                            taxYear: TaxYear,
                            index: Int): Result = {
    val updatedMode = if (hasAllFurtherAnswers(businessId, userAnswers)) originalMode else NormalMode
    val newPage: Call = updatedMode match {
      case NormalMode if answer => routes.ContractStartDateController.onPageLoad(taxYear, businessId, index, NormalMode)
      case NormalMode           => routes.ConstructionStartDateController.onPageLoad(taxYear, businessId, index, NormalMode)
      case CheckMode            => cyaPage(taxYear, businessId)
    }

    Redirect(newPage)
  }
}
