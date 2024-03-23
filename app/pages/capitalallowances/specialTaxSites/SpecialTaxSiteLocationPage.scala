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
import models.journeys.capitalallowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

object SpecialTaxSiteLocationPage extends SpecialTaxSitesBasePage[SpecialTaxSiteLocation] {
  override def toString: String = "specialTaxSiteLocation"

  def hasAllFurtherAnswers(site: NewSpecialTaxSite): Boolean =
    site.specialTaxSiteLocation.isDefined && NewSiteClaimingAmountPage.hasAllFurtherAnswers(site)

  override def nextPageWithIndex(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear, index: Int): Result =
    getSiteFromIndex(userAnswers, businessId, index) match {
      case None => redirectToRecoveryPage("NewSpecialTaxSitesList data not found when redirecting from SpecialTaxSiteLocationPage")
      case Some(site) =>
        Redirect(
          if (hasAllFurtherAnswers(site)) routes.SiteSummaryController.onPageLoad(taxYear, businessId, index)
          else routes.NewSiteClaimingAmountController.onPageLoad(taxYear, businessId, index, NormalMode)
        )
    }
}
