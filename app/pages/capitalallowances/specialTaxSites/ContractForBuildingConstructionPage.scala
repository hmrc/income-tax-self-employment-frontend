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
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

object ContractForBuildingConstructionPage extends SpecialTaxSitesBasePage[Boolean] {
  override def toString: String = "contractForBuildingConstruction"

  def hasAllFurtherAnswers(site: NewSpecialTaxSite): Boolean = site.contractForBuildingConstruction.isDefined &&
    (ConstructionStartDatePage.hasAllFurtherAnswers(site) || ContractStartDatePage.hasAllFurtherAnswers(site))

  def nextPageWithIndex(answer: Boolean, mode: Mode, userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear, index: Int): Result =
    getSiteFromIndex(userAnswers, businessId, index) match {
      case None => redirectToRecoveryPage(s"Site of index $index not found when redirecting from ContractForBuildingConstructionPage")
      case Some(site) =>
        Redirect(mode match {
          case CheckMode if hasAllFurtherAnswers(site) => routes.SiteSummaryController.onPageLoad(taxYear, businessId, index)
          case _ if answer                             => routes.ContractStartDateController.onPageLoad(taxYear, businessId, index, NormalMode)
          case _                                       => routes.ConstructionStartDateController.onPageLoad(taxYear, businessId, index, NormalMode)
        })
    }
}
