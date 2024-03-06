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
    (Some(BigDecimal(100.0)), Some(-120), Some(0)),
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
