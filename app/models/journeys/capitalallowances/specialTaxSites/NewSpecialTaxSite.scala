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

import cats.implicits.catsSyntaxOptionId
import play.api.libs.json.{Json, OFormat}
import utils.MoneyUtils.formatMoney

import java.time.LocalDate

case class NewSpecialTaxSite(contractForBuildingConstruction: Option[Boolean] = None,
                             contractStartDate: Option[LocalDate] = None,
                             constructionStartDate: Option[LocalDate] = None,
                             qualifyingUseStartDate: Option[LocalDate] = None,
                             qualifyingExpenditure: Option[BigDecimal] = None,
                             specialTaxSiteLocation: Option[SpecialTaxSiteLocation] = None,
                             newSiteClaimingAmount: Option[BigDecimal] = None) {
  def isComplete: Boolean =
    contractForBuildingConstruction.isDefined &&
      (contractStartDate.isDefined || constructionStartDate.isDefined) &&
      qualifyingUseStartDate.isDefined &&
      qualifyingExpenditure.isDefined &&
      specialTaxSiteLocation.isDefined &&
      newSiteClaimingAmount.isDefined

  def isEmpty: Boolean = Seq(
    contractForBuildingConstruction,
    contractStartDate,
    constructionStartDate,
    qualifyingUseStartDate,
    qualifyingExpenditure,
    specialTaxSiteLocation,
    newSiteClaimingAmount
  ).forall(_.isEmpty)

}

object NewSpecialTaxSite {
  implicit val formats: OFormat[NewSpecialTaxSite] = Json.format[NewSpecialTaxSite]

  def newSite: NewSpecialTaxSite = NewSpecialTaxSite()

  def returnTotalIfMultipleSites(sites: List[NewSpecialTaxSite]): Option[String] =
    if (sites.length > 1) formatMoney(sites.map(_.newSiteClaimingAmount.getOrElse(BigDecimal(0))).sum).some
    else None
}
