/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSE
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object ZegvUseOutsideSEFormProvider extends Mappings {

  case class ZegvUseOutsideSEFormModel(radioPercentage: ZegvUseOutsideSE, optDifferentAmount: Int = 0)

  private val radioPercentage    = "radioPercentage"
  private val optDifferentAmount = "optDifferentAmount"
  private val maxPercentage      = 100
  private val minPercentage      = 0

  def apply(userType: UserType)(implicit messages: Messages): Form[ZegvUseOutsideSEFormModel] = {
    val requiredRadioError  = s"zegvUseOutsideSE.error.required.$userType"
    val requiredAmountError = "error.required"
    val overMaxError        = "zegvUseOutsideSE.error.overMax"
    val lessThanZeroError   = "error.lessThanZero"
    val nonNumericError     = "error.nonNumeric"
    val noDecimalsError     = "error.nonDecimal"

    def validateRadio(): Mapping[ZegvUseOutsideSE] = enumerable[ZegvUseOutsideSE](messages(s"$requiredRadioError"))

    def validateInt(): Mapping[Int] = int(requiredAmountError, noDecimalsError, nonNumericError)
      .verifying(greaterThan(minPercentage, lessThanZeroError))
      .verifying(lessThan(maxPercentage, overMaxError))

    Form[ZegvUseOutsideSEFormModel](
      mapping(
        radioPercentage    -> validateRadio(),
        optDifferentAmount -> validateInt()
      )(ZegvUseOutsideSEFormModel.apply)(ZegvUseOutsideSEFormModel.unapply)
    )
  }

}
