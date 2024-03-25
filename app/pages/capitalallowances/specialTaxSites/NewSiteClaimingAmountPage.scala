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
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

object NewSiteClaimingAmountPage extends SpecialTaxSitesBasePage[BigDecimal] {
  override def toString: String = "newSiteClaimingAmount"

  def hasAllFurtherAnswers(site: NewSpecialTaxSite): Boolean = site.newSiteClaimingAmount.isDefined

  def nextPage(businessId: BusinessId, taxYear: TaxYear): Result = Redirect(routes.NewTaxSitesController.onPageLoad(taxYear, businessId))
}
