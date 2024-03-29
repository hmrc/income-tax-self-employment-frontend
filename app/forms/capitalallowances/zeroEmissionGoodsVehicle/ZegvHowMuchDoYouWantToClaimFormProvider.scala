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

import forms._
import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages
import uk.gov.voa.play.form.ConditionalMappings._

object ZegvHowMuchDoYouWantToClaimFormProvider extends Mappings with MoneyBounds {

  case class ZegvHowMuchDoYouWantToClaimModel(howMuchDoYouWantToClaim: ZegvHowMuchDoYouWantToClaim, totalCost: Option[BigDecimal])

  private[zeroEmissionGoodsVehicle] val standardErrors = FormStandardErrors("zegvHowMuchDoYouWantToClaim")
  private val howMuchDoYouWantToClaim                  = "howMuchDoYouWantToClaim"
  private val totalCost                                = "totalCost"

  def apply(userType: UserType, fullAmount: BigDecimal)(implicit messages: Messages): Form[ZegvHowMuchDoYouWantToClaimModel] = {
    import standardErrors._
    val validatedRadio = enumerable[ZegvHowMuchDoYouWantToClaim](messages(requiredError(userType)))

    def validateAmount(fullAmount: BigDecimal): Mapping[BigDecimal] = currency(amountRequiredError, NonNumericError)
      .verifying(greaterThan(minimumValue, LessThanZeroError))
      .verifying(maximumValue(fullAmount, overMaxCustomMessageError))
      .verifying(regexpBigDecimal(noDecimalRegexp, NoDecimalsError))

    Form(
      mapping(
        howMuchDoYouWantToClaim -> validatedRadio,
        totalCost -> mandatoryIf(isEqual(howMuchDoYouWantToClaim, ZegvHowMuchDoYouWantToClaim.LowerAmount.toString), validateAmount(fullAmount))
      )(ZegvHowMuchDoYouWantToClaimModel.apply)(ZegvHowMuchDoYouWantToClaimModel.unapply)
    )
  }

}
