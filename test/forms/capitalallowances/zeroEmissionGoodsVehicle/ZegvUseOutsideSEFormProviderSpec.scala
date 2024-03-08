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

package forms.capitalallowances.zeroEmissionGoodsVehicle

import forms.behaviours.SimpleFieldBehaviours
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEFormProvider.ZegvUseOutsideSEFormModel
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSE
import play.api.data.FormError
import play.api.i18n.Messages

class ZegvUseOutsideSEFormProviderSpec extends SimpleFieldBehaviours {
  implicit val msg: Messages = messagesStubbed

  "ZegvTotalCostOfVehicleFormProvider" - forAllUserTypes { userType =>
    implicit val form = ZegvUseOutsideSEFormProvider.apply(userType)
    checkValidInstance(ZegvUseOutsideSEFormModel(ZegvUseOutsideSE.Ten, 1))
    checkValidInstance(ZegvUseOutsideSEFormModel(ZegvUseOutsideSE.TwentyFive, 1))
    checkValidInstance(ZegvUseOutsideSEFormModel(ZegvUseOutsideSE.Fifty, 1))
    checkValidInstance(ZegvUseOutsideSEFormModel(ZegvUseOutsideSE.DifferentAmount, 33))

    checkMandatoryForm(
      FormError("radioPercentage", s"zegvUseOutsideSE.error.required.$userType"),
      FormError("optDifferentAmount", "error.required")
    )

  }

  "ZegvTotalCostOfVehicleFormProvider custom validation" - forAllUserTypes { userType =>
    "not bind if percentage is not valid" in {
      val actual = ZegvUseOutsideSEFormProvider
        .apply(userType)
        .bind(
          Map(
            "radioPercentage"    -> "200%",
            "optDifferentAmount" -> "10"
          ))

      actual.errors.toList mustEqual List(FormError("radioPercentage", "error.invalid"))
    }

    "not bind if opt different amount is less than 0%" in {
      val actual = ZegvUseOutsideSEFormProvider
        .apply(userType)
        .bind(
          Map(
            "radioPercentage"    -> "50%",
            "optDifferentAmount" -> "-1"
          ))

      actual.errors.toList mustEqual List(FormError("optDifferentAmount", "error.lessThanZero", List(0)))
    }

    "not bind if opt different amount is greater than 99%" in {
      val actual = ZegvUseOutsideSEFormProvider
        .apply(userType)
        .bind(
          Map(
            "radioPercentage"    -> "50%",
            "optDifferentAmount" -> "101"
          ))

      actual.errors.toList mustEqual List(FormError("optDifferentAmount", "error.maxNinetyNine", List(100)))
    }
  }

}
