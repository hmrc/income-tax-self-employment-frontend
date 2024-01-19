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

package viewsupport.journeys.capitalallowances.tailoring

import models.journeys.capitalallowances.tailoring.CapitalAllowances.{AnnualInvestment, ZeroEmissionCar, ZeroEmissionGoodsVehicle}
import models.journeys.capitalallowances.tailoring.Group
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import viewsupport.journeys.capitalallowances.tailoring.SelectCapitalAllowancesViewSupport.sortByAllowanceGroups

class SelectCapitalAllowancesViewSupportSpec extends AnyWordSpec with Matchers {

  val allowances = List(ZeroEmissionCar, ZeroEmissionGoodsVehicle, AnnualInvestment)

  "Sorting allowances by groups" must {
    "enforce the specified ordering" in {
      val resultingKeys = sortByAllowanceGroups(allowances).keySet

      resultingKeys should contain theSameElementsInOrderAs Set(Group.ZeroEmissions, Group.AssetAndAllowances)
    }
    "assign unique indexes to each allowance" in {
      val resultingIndexes: List[Int] =
        sortByAllowanceGroups(allowances).values
          .flatMap(allowances => allowances.map(_._2))
          .toList

      resultingIndexes.distinct.size shouldBe resultingIndexes.size
    }
  }
}
