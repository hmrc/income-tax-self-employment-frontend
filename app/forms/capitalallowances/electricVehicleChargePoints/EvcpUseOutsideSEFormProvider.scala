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

package forms.capitalallowances.electricVehicleChargePoints

import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSE
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object EvcpUseOutsideSEFormProvider extends Mappings {

  case class EvcpUseOutsideSEFormModel(radioPercentage: EvcpUseOutsideSE, optDifferentAmount: Int = 0)

  private val radioPercentage    = "radioPercentage"
  private val optDifferentAmount = "optDifferentAmount"
  private val maxPercentage      = 99
  private val minPercentage      = 1

  def apply(userType: UserType)(implicit messages: Messages): Form[EvcpUseOutsideSEFormModel] = {
    val requiredRadioError  = s"evcpUseOutsideSE.error.required.$userType"
    val requiredAmountError = "error.required"
    val overMaxError        = "error.maxNinetyNine"
    val lessThanZeroError   = "error.lessThanZero"
    val nonNumericError     = "error.nonNumeric"
    val noDecimalsError     = "error.nonDecimal"

    def validateRadio(): Mapping[EvcpUseOutsideSE] = enumerable[EvcpUseOutsideSE](messages(s"$requiredRadioError"))

    def validateInt(): Mapping[Int] = int(requiredAmountError, noDecimalsError, nonNumericError)
      .verifying(minimumValue(minPercentage, lessThanZeroError))
      .verifying(maximumValue(maxPercentage, overMaxError))

    Form[EvcpUseOutsideSEFormModel](
      mapping(
        radioPercentage    -> validateRadio(),
        optDifferentAmount -> validateInt()
      )(EvcpUseOutsideSEFormModel.apply)(EvcpUseOutsideSEFormModel.unapply)
    )
  }

}
