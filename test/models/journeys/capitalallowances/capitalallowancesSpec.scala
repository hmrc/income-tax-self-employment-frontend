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

import base.SpecBase.{businessId, emptyUserAnswers, fakeDataRequest}
import org.scalatest.TryValues._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle.{ZegvTotalCostOfVehiclePage, ZegvUseOutsideSEPercentagePage}
import queries.Settable.SetAnswer

class capitalallowancesSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {

  val cases = Table(
    ("total", "percentage", "expected"),
    (None, None, None),
    (Some(BigDecimal(100.0)), None, Some(100)),
    (None, Some(50), None),
    (Some(BigDecimal(100.0)), Some(50), Some(50)),
    (Some(BigDecimal(101.0)), Some(50), Some(51)),
    (Some(BigDecimal(100.0)), Some(100), Some(0)),
    (Some(BigDecimal(100.0)), Some(120), Some(0)),
    (Some(BigDecimal(100.0)), Some(-120), Some(0))
  )

  "calculateFullCost" should {
    "return None if no answer" in forAll(cases) { case (total, percentage, expected) =>
      val cmds =
        total.map(t => List(SetAnswer(ZegvTotalCostOfVehiclePage, t))).getOrElse(Nil) ++
          percentage.map(p => List(SetAnswer(ZegvUseOutsideSEPercentagePage, p))).getOrElse(Nil)

      val userAnswers = SetAnswer
        .setMany(businessId, emptyUserAnswers)(
          cmds: _*
        )
        .success
        .value

      val fakeRequest = fakeDataRequest(userAnswers)

      val actual = calculateFullCost(ZegvUseOutsideSEPercentagePage, ZegvTotalCostOfVehiclePage, fakeRequest, businessId)
      assert(actual === expected)
    }
  }
}
