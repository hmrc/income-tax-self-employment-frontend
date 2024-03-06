package viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase._
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider._
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim.{FullCost, LowerAmount}
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import queries.Settable
import queries.Settable.SetAnswer
import viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimViewModel._

class ZegvHowMuchDoYouWantToClaimViewModelSpec extends AnyWordSpecLike {

  "createFillForm" should {
    "return None when no data" in {
      val result = createFillForm(fakeDataRequest(emptyUserAnswers), businessId)(messagesStubbed)
      assert(result === None)
    }

    "return form for the lower amount claim" in {
      val answers = Settable.SetAnswer
        .setMany(businessId, emptyUserAnswers)(
          SetAnswer(ZegvTotalCostOfVehiclePage, BigDecimal(100)),
          SetAnswer(ZegvHowMuchDoYouWantToClaimPage, LowerAmount),
          SetAnswer(ZegvClaimAmountPage, BigDecimal(50))
        )
        .success
        .value

      val (actualForm, actualFullCost) = createFillForm(fakeDataRequest(answers), businessId)(messagesStubbed).value
      assert(actualForm.value.value === ZegvHowMuchDoYouWantToClaimModel(LowerAmount, Some(BigDecimal(50))))
      assert(actualFullCost === 100)
    }

    "return form for the full cost claim" in {
      val answers = Settable.SetAnswer
        .setMany(businessId, emptyUserAnswers)(
          SetAnswer(ZegvTotalCostOfVehiclePage, BigDecimal(100)),
          SetAnswer(ZegvHowMuchDoYouWantToClaimPage, FullCost),
          SetAnswer(ZegvClaimAmountPage, BigDecimal(50))
        )
        .success
        .value

      val (actualForm, actualFullCost) = createFillForm(fakeDataRequest(answers), businessId)(messagesStubbed).value
      assert(actualForm.value.value === ZegvHowMuchDoYouWantToClaimModel(FullCost, None))
      assert(actualFullCost === 100)
    }

    "return an empty form if no claim selected" in {
      val answers = Settable.SetAnswer
        .setMany(businessId, emptyUserAnswers)(
          SetAnswer(ZegvTotalCostOfVehiclePage, BigDecimal(100)),
          SetAnswer(ZegvClaimAmountPage, BigDecimal(50))
        )
        .success
        .value

      val (actualForm, actualFullCost) = createFillForm(fakeDataRequest(answers), businessId)(messagesStubbed).value
      assert(actualForm.value === None)
      assert(actualFullCost === 100)
    }
  }
}
