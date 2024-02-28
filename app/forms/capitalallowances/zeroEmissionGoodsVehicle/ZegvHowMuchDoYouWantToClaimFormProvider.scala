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
import models.common.{MoneyBounds, UserType}
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object ZegvHowMuchDoYouWantToClaimFormProvider extends Mappings with MoneyBounds {

  case class ZegvHowMuchDoYouWantToClaimModel(howMuchDoYouWantToClaim: ZegvHowMuchDoYouWantToClaim, totalCost: BigDecimal = 0)

  private val howMuchDoYouWantToClaim = "howMuchDoYouWantToClaim"
  private val totalCost               = "totalCost"

  def apply(userType: UserType, fullAmount: BigDecimal)(implicit messages: Messages): Form[ZegvHowMuchDoYouWantToClaimModel] = {
    val requiredError       = s"zegvHowMuchDoYouWantToClaim.error.required.$userType"
    val amountRequiredError = "zegvHowMuchDoYouWantToClaim.error.required.amount"
    val lessThanZeroError   = "error.lessThanZero"
    val nonNumericError     = "error.nonNumeric"
    val noDecimalsError     = "error.nonDecimal"
    val overMaxError        = "zegvHowMuchDoYouWantToClaim.error.overMax"

    def validateRadio(): Mapping[ZegvHowMuchDoYouWantToClaim] = enumerable[ZegvHowMuchDoYouWantToClaim](messages(requiredError))

    def validateAmount(fullAmount: BigDecimal): Mapping[BigDecimal] = currency(amountRequiredError, nonNumericError)
      .verifying(greaterThan(minimumValue, lessThanZeroError))
      .verifying(maximumValue(fullAmount, overMaxError))
      .verifying(regexpBigDecimal(noDecimalRegexp, noDecimalsError))

    Form[ZegvHowMuchDoYouWantToClaimModel](
      mapping(
        howMuchDoYouWantToClaim -> validateRadio(),
        totalCost               -> validateAmount(fullAmount)
      )(ZegvHowMuchDoYouWantToClaimModel.apply)(ZegvHowMuchDoYouWantToClaimModel.unapply)
    )
  }

}
