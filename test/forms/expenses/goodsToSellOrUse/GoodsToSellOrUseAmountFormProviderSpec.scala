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

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form

class GoodsToSellOrUseAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("GoodsToSellOrUseAmountFormProvider") {

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new GoodsToSellOrUseAmountFormProvider()(userType)

  override lazy val requiredError: String     = "goodsToSellOrUseAmount.error.required"
  override lazy val nonNumericError: String   = "goodsToSellOrUseAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String = "goodsToSellOrUseAmount.error.lessThanZero"
  override lazy val overMaxError: String      = "goodsToSellOrUseAmount.error.overMax"

}
