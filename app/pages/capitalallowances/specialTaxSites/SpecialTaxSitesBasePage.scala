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
import controllers.redirectJourneyRecovery
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import models.requests.DataRequest
import pages.OneQuestionPage
import play.api.data.Form
import play.api.mvc.{Call, Result}

trait SpecialTaxSitesBasePage[A] extends OneQuestionPage[A] {
  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.SpecialTaxSitesCYAController.onPageLoad(taxYear, businessId)

  def getSiteFromIndex(request: DataRequest[_], businessId: BusinessId, index: Int): Option[NewSpecialTaxSite] =
    request.getValue(NewSpecialTaxSitesList, businessId).map(_(index))

  def getSiteOrRedirect(request: DataRequest[_], businessId: BusinessId, index: Int): Either[Result, NewSpecialTaxSite] =
    getSiteFromIndex(request, businessId, index).toRight(redirectJourneyRecovery())

  def fillFormWithIndex[B](form: Form[B], page: SpecialTaxSitesBasePage[B], request: DataRequest[_], businessId: BusinessId, index: Int): Form[B] = {
    val site = getSiteFromIndex(request, businessId, index)
    val existingValue: Option[B] = page match {
      case ContractForBuildingConstructionPage => site.flatMap(_.contractForBuildingConstruction)
      case ContractStartDatePage               => site.flatMap(_.contractStartDate)
      case ConstructionStartDatePage           => site.flatMap(_.constructionStartDate)
      case QualifyingUseStartDatePage          => site.flatMap(_.qualifyingUseStartDate)
      case SpecialTaxSiteLocationPage          => site.flatMap(_.specialTaxSiteLocation)
      case NewSiteClaimingAmountPage           => site.flatMap(_.newSiteClaimingAmount)
      case _                                   => ???
    }
    existingValue.fold(form)(form.fill)
  }
}
