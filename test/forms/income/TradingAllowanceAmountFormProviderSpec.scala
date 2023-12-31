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

package forms.income

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form

class TradingAllowanceAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("TradingAllowanceAmountFormProvider") {

  private lazy val turnoverAmount = 1000.00

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new TradingAllowanceAmountFormProvider()(userType, turnoverAmount)

  override lazy val requiredError: String     = "tradingAllowanceAmount.error.required"
  override lazy val nonNumericError: String   = "tradingAllowanceAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String = "tradingAllowanceAmount.error.lessThanZero"
  override lazy val overMaxError: String      = "tradingAllowanceAmount.error.overTurnover"
  override lazy val maximum: BigDecimal       = turnoverAmount
  override lazy val validDataGenerator        = currencyInRangeWithCommas(minimum, turnoverAmount)

}
