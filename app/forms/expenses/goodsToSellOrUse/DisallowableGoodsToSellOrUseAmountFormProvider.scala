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

package forms.expenses.goodsToSellOrUse

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class DisallowableGoodsToSellOrUseAmountFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal("disallowableGoodsToSellOrUseAmount.error.required", "disallowableGoodsToSellOrUseAmount.error.nonNumeric")
        .verifying(isBigDecimalGreaterThanZero("disallowableGoodsToSellOrUseAmount.error.lessThanZero"))
        .verifying(isBigDecimalLessThanMax(100000000000.00, "disallowableGoodsToSellOrUseAmount.error.overMax"))
    )

}