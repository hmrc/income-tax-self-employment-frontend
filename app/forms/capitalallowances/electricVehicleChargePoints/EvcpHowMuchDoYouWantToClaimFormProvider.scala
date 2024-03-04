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
import models.common.{MoneyBounds, UserType}
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpHowMuchDoYouWantToClaim
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object EvcpHowMuchDoYouWantToClaimFormProvider extends Mappings with MoneyBounds {

  case class EvcpHowMuchDoYouWantToClaimModel(howMuchDoYouWantToClaim: EvcpHowMuchDoYouWantToClaim, totalCost: BigDecimal = 0)

  private val howMuchDoYouWantToClaim = "howMuchDoYouWantToClaim"
  private val totalCost               = "totalCost"

  def apply(userType: UserType, fullAmount: BigDecimal)(implicit messages: Messages): Form[EvcpHowMuchDoYouWantToClaimModel] = {
    val requiredError       = s"evcpHowMuchDoYouWantToClaim.error.required.$userType"
    val amountRequiredError = "evcpHowMuchDoYouWantToClaim.error.required.amount"
    val lessThanZeroError   = "error.lessThanZero"
    val nonNumericError     = "error.nonNumeric"
    val noDecimalsError     = "error.nonDecimal"
    val overMaxError        = "evcpHowMuchDoYouWantToClaim.error.overMax"

    def validateRadio(): Mapping[EvcpHowMuchDoYouWantToClaim] = enumerable[EvcpHowMuchDoYouWantToClaim](messages(requiredError))

    def validateAmount(fullAmount: BigDecimal): Mapping[BigDecimal] = currency(amountRequiredError, nonNumericError)
      .verifying(greaterThan(minimumValue, lessThanZeroError))
      .verifying(maximumValue(fullAmount, overMaxError))
      .verifying(regexpBigDecimal(noDecimalRegexp, noDecimalsError))

    Form[EvcpHowMuchDoYouWantToClaimModel](
      mapping(
        howMuchDoYouWantToClaim -> validateRadio(),
        totalCost               -> validateAmount(fullAmount)
      )(EvcpHowMuchDoYouWantToClaimModel.apply)(EvcpHowMuchDoYouWantToClaimModel.unapply)
    )
  }

}
