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

package models.journeys.capitalallowances

import base.SpecBase
import cats.implicits.catsSyntaxOptionId
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesAnswers.removeIncompleteSites
import models.journeys.capitalallowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation}

import java.time.LocalDate

class SpecialTaxSitesAnswersSpec extends SpecBase {

  val completedSite: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    LocalDate.now().some,
    LocalDate.now().some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )
  val validIncompleteSite1: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    None,
    LocalDate.now().some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )
  val validIncompleteSite2: NewSpecialTaxSite = NewSpecialTaxSite(
    false.some,
    None,
    LocalDate.now().some,
    LocalDate.now().some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )
  val incompleteSite1: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    LocalDate.now().some,
    None,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )
  val incompleteSite2: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    LocalDate.now().some,
    LocalDate.now().some,
    None,
    BigDecimal(10000).some
  )
  val incompleteSite3: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    LocalDate.now().some,
    LocalDate.now().some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    None
  )

  val mixedList: List[NewSpecialTaxSite] =
    List(completedSite, incompleteSite1, validIncompleteSite1, incompleteSite2, validIncompleteSite2, incompleteSite3)
  val expectedCleanList: List[NewSpecialTaxSite] = List(completedSite, validIncompleteSite1, validIncompleteSite2)

  "removeIncompleteSites" - {
    "remove any incomplete sites from list and return it" in {
      removeIncompleteSites(mixedList) == expectedCleanList
    }
  }
}
