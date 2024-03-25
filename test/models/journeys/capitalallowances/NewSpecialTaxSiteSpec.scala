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
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.newSite
import models.journeys.capitalallowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation}

import java.time.LocalDate

class NewSpecialTaxSiteSpec extends SpecBase {

  val completedSite: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    LocalDate.now().some,
    LocalDate.now().some,
    LocalDate.now().some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )

  "isComplete" - {
    "return true if all necessary fields are filled" in {
      val validCompleted = List(completedSite, completedSite.copy(contractStartDate = None), completedSite.copy(constructionStartDate = None))
      validCompleted.forall(_.isComplete)
    }
    "return false if any necessary fields are not filled" in {
      val notValidCompleted =
        List(newSite, completedSite.copy(contractStartDate = None, constructionStartDate = None), completedSite.copy(specialTaxSiteLocation = None))
      notValidCompleted.forall(!_.isComplete)
    }
  }

  "isEmpty" - {
    "return true if all fields are empty" in {
      newSite.isEmpty
    }
    "return false if any fields are nonEmpty" in {
      val notValidCompleted = List(newSite.copy(contractStartDate = LocalDate.now().some), completedSite)
      notValidCompleted.forall(!_.isEmpty)
    }
  }
}
