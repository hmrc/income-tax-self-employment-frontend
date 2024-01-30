/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.expenses.financialCharges

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form
import utils.MoneyUtils.formatMoney

class FinancialChargesDisallowableAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("FinancialChargesDisallowableAmountFormProvider") {

  private lazy val amount = 123.00

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new FinancialChargesDisallowableAmountFormProvider()(userType, amount)

  override lazy val requiredError: String                  = "financialChargesDisallowableAmount.error.required"
  override lazy val nonNumericError: String                = "financialChargesDisallowableAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String              = "financialChargesDisallowableAmount.error.lessThanZero"
  override lazy val overMaxError: String                   = "financialChargesDisallowableAmount.error.overAmount"
  override lazy val optionalArgumentsAll: Option[Seq[String]] = Some(Seq(formatMoney(amount)))
  override lazy val maximum: BigDecimal                    = amount
}
