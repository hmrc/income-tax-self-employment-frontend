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
