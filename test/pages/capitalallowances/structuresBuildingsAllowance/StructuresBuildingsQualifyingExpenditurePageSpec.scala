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

package pages.capitalallowances.structuresBuildingsAllowance

import base.SpecBase.{buildUserAnswers, businessId, emptyUserAnswers, taxYear}
import controllers.journeys.capitalallowances.structuresBuildingsAllowance.routes
import models.NormalMode
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.{NewStructureBuilding, StructuresBuildingsLocation}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{OptionValues, TryValues}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.Helpers._

import java.time.LocalDate

class StructuresBuildingsQualifyingExpenditurePageSpec extends AnyWordSpecLike with TryValues with OptionValues with Matchers {

  "navigation" should {
    "navigate to Location page" in {
      def baseAnswers: UserAnswers = buildUserAnswers(NewStructuresBuildingsList, List(NewStructureBuilding(Some(LocalDate.of(2020, 2, 2)))))

      val result: Result = StructuresBuildingsQualifyingExpenditurePage.nextPageWithIndex(baseAnswers, businessId, taxYear, 0)
      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe routes.StructuresBuildingsLocationController.onPageLoad(taxYear, businessId, 0, NormalMode).url
    }

    "navigate to CYA page when NewStructureBuilding is complete" in {
      def baseAnswers: UserAnswers = buildUserAnswers(
        NewStructuresBuildingsList,
        List(
          NewStructureBuilding(
            Some(LocalDate.of(2020, 2, 2)),
            Some(BigDecimal(200)),
            Some(StructuresBuildingsLocation(Some("name"), Some("number"), "GU84NB")),
            Some(BigDecimal(100))))
      )

      val result: Result = StructuresBuildingsQualifyingExpenditurePage.nextPageWithIndex(baseAnswers, businessId, taxYear, 0)
      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe routes.StructuresBuildingsSummaryController.onPageLoad(taxYear, businessId, 0).url
    }
  }

}
