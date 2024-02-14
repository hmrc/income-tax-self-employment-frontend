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
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE.DifferentAmount
import play.api.data.Forms.{ignored, mapping}
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object ZecUseOutsideSEFormProvider extends Mappings {

  case class ZecUseOutsideSEFormModel(radioPercentage: ZecUseOutsideSE, optDifferentAmount: Option[Int] = None)

  private val radioPercentage    = "radioPercentage"
  private val optDifferentAmount = "optDifferentAmount"

  def apply()(implicit messages: Messages): Form[ZecUseOutsideSEFormModel] = {
    val requiredRadioError  = "---- add error for no radio input ----"
    val requiredNumberError = "---- add error for no number input ----"
    val nonNumericError     = "---- add error for non numeric input ----"

    def validateRadio(): Mapping[ZecUseOutsideSE] =
      enumerable[ZecUseOutsideSE](messages(s"$requiredRadioError"))

    def validateOptionalInt(valueKey: String, conditionalValue: String): Mapping[Option[Int]] =
      if (conditionalValue == DifferentAmount.toString) {
        int(messages(s"$requiredNumberError$valueKey"), messages(nonNumericError)).transform(Some.apply, _.getOrElse(0))
      } else {
        ignored(None: Option[Int])
      }

    Form[ZecUseOutsideSEFormModel](
      mapping(
        radioPercentage    -> validateRadio(),
        optDifferentAmount -> validateOptionalInt(optDifferentAmount, radioPercentage)
      )(ZecUseOutsideSEFormModel.apply)(ZecUseOutsideSEFormModel.unapply)
    )
  }

}
