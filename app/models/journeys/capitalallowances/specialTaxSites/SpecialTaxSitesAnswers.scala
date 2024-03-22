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

package models.journeys.capitalallowances.specialTaxSites

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class SpecialTaxSitesAnswers(specialTaxSites: Boolean, newSpecialTaxSites: List[NewSpecialTaxSite])

object SpecialTaxSitesAnswers {
  implicit val formats: Format[SpecialTaxSitesAnswers] = Json.format[SpecialTaxSitesAnswers]
}

case class NewSpecialTaxSite(contractForBuildingConstruction: Option[Boolean] = None,
                             contractStartDate: Option[LocalDate] = None,
                             constructionStartDate: Option[LocalDate] = None,
                             qualifyingUseStartDate: Option[LocalDate] = None,
                             specialTaxSiteLocation: Option[SpecialTaxSiteLocation] = None,
                             newSiteClaimingAmount: Option[BigDecimal] = None){
  def isComplete(): Boolean = contractForBuildingConstruction.isDefined && qualifyingUseStartDate.isDefined //TODO fix this method
  // all values will need to be options and you add them as you go. If in check mode or by the end of the loop you will check if it is complete, otherwise you will error
  // the List of NewTaxSites will need a method to self clean any half filled Sites
  // Will need to check that all pages have the correct future and previous page checks, dependent pages, and correct CYAs
}

object NewSpecialTaxSite {
  implicit val formats: Format[NewSpecialTaxSite] = Json.format[NewSpecialTaxSite]

  def newSite(): NewSpecialTaxSite = NewSpecialTaxSite()
}
