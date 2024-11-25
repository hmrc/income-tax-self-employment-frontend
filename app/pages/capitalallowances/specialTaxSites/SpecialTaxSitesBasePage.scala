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

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.newSite
import models.requests.DataRequest
import pages.OneQuestionPage
import play.api.data.Form
import play.api.mvc.{Call, Result}

trait SpecialTaxSitesBasePage[A] extends OneQuestionPage[A] {
  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.SpecialTaxSitesCYAController.onPageLoad(taxYear, businessId)

  def getSiteFromIndex(userAnswers: UserAnswers, businessId: BusinessId, index: Int): Option[NewSpecialTaxSite] =
    userAnswers.get(NewSpecialTaxSitesList, businessId.some).map(list => if (list.length > index) list(index) else newSite)

  def fillFormWithIndex[B](form: Form[B], page: SpecialTaxSitesBasePage[B], request: DataRequest[_], businessId: BusinessId, index: Int): Form[B] = {
    val existingSite: Option[NewSpecialTaxSite] = getSiteFromIndex(request.userAnswers, businessId, index)
    val existingValue: Option[B] = page match {
      case ContractForBuildingConstructionPage => existingSite.flatMap(_.contractForBuildingConstruction)
      case ContractStartDatePage               => existingSite.flatMap(_.contractStartDate)
      case ConstructionStartDatePage           => existingSite.flatMap(_.constructionStartDate)
      case QualifyingUseStartDatePage          => existingSite.flatMap(_.qualifyingUseStartDate)
      case QualifyingExpenditurePage           => existingSite.flatMap(_.qualifyingExpenditure)
      case SpecialTaxSiteLocationPage          => existingSite.flatMap(_.specialTaxSiteLocation)
      case NewSiteClaimingAmountPage           => existingSite.flatMap(_.newSiteClaimingAmount)
      case _                                   => None
    }
    existingValue.fold(form)(form.fill)
  }

  def nextPageWithIndex(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear, index: Int): Result = ???
}
