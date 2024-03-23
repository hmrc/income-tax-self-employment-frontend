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

  def removeIncompleteSites(sitesList: List[NewSpecialTaxSite]): List[NewSpecialTaxSite] = sitesList.filter(_.isComplete)
}

case class NewSpecialTaxSite(contractForBuildingConstruction: Option[Boolean] = None,
                             contractStartDate: Option[LocalDate] = None,
                             constructionStartDate: Option[LocalDate] = None,
                             qualifyingUseStartDate: Option[LocalDate] = None,
                             specialTaxSiteLocation: Option[SpecialTaxSiteLocation] = None,
                             newSiteClaimingAmount: Option[BigDecimal] = None) {
  def isComplete: Boolean =
    contractForBuildingConstruction.isDefined &&
      (contractStartDate.isDefined || constructionStartDate.isDefined) &&
      qualifyingUseStartDate.isDefined &&
      specialTaxSiteLocation.isDefined &&
      newSiteClaimingAmount.isDefined

  def isEmpty: Boolean = Seq(
    contractForBuildingConstruction,
    contractStartDate,
    constructionStartDate,
    qualifyingUseStartDate,
    specialTaxSiteLocation,
    newSiteClaimingAmount
  ).forall(_.isEmpty)

}

object NewSpecialTaxSite {
  implicit val formats: Format[NewSpecialTaxSite] = Json.format[NewSpecialTaxSite]

  def newSite: NewSpecialTaxSite = NewSpecialTaxSite()
}
