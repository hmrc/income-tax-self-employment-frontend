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
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.redirectOnBoolean
import play.api.mvc.Call
import queries.Settable

object ContinueClaimingAllowanceForExistingSitePage extends SpecialTaxSitesBasePage[Boolean] {

  override def toString: String = "continueClaimingAllowanceForExistingSite"

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers
      .get(this, businessId)
      .exists(
        !_ || ExistingSiteClaimingAmountPage.hasAllFurtherAnswers(businessId, userAnswers)
      )

  override val dependentPagesWhenNo: List[Settable[_]] = List(ExistingSiteClaimingAmountPage)

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    redirectOnBoolean(
      this,
      userAnswers,
      businessId,
      onTrue = routes.ExistingSiteClaimingAmountController.onPageLoad(taxYear, businessId, NormalMode),
      onFalse = cyaPage(taxYear, businessId)
    )
}
