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

package forms.capitalallowances.zeroEmissionCars

import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object ZecUseOutsideSEFormProvider extends Mappings {

  case class ZecUseOutsideSEFormModel(radioPercentage: ZecUseOutsideSE, optDifferentAmount: Int = 0)

  private val radioPercentage    = "radioPercentage"
  private val optDifferentAmount = "optDifferentAmount"

  def apply(userType: UserType)(implicit messages: Messages): Form[ZecUseOutsideSEFormModel] = {
    val requiredRadioError = s"zecUseOutsideSE.error.required.$userType"
    val overMaxError       = "zecUseOutsideSE.error.overMax"
    val lessThanZeroError  = "zecUseOutsideSE.error.lessThanZero"

    def validateRadio(): Mapping[ZecUseOutsideSE] = enumerable[ZecUseOutsideSE](messages(s"$requiredRadioError"))

    def validateInt(): Mapping[Int] = int()
      .verifying(minimumValue(0, lessThanZeroError))
      .verifying(lessThan(100, overMaxError))

    Form[ZecUseOutsideSEFormModel](
      mapping(
        radioPercentage    -> validateRadio(),
        optDifferentAmount -> validateInt()
      )(ZecUseOutsideSEFormModel.apply)(ZecUseOutsideSEFormModel.unapply)
    )
  }

}
