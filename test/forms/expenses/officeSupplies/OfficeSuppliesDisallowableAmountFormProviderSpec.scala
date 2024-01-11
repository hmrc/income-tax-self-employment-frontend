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

package forms.expenses.officeSupplies

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form
import utils.MoneyUtils.formatMoney

class OfficeSuppliesDisallowableAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("OfficeSuppliesDisallowableAmountFormProvider") {

  private lazy val amount       = 5623.50
  private lazy val amountString = formatMoney(amount, addDecimalForWholeNumbers = false)

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new OfficeSuppliesDisallowableAmountFormProvider()(userType, amount)

  override lazy val requiredError: String                  = "officeSuppliesDisallowableAmount.error.required"
  override lazy val nonNumericError: String                = "officeSuppliesDisallowableAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String              = "officeSuppliesDisallowableAmount.error.lessThanZero"
  override lazy val overMaxError: String                   = "officeSuppliesDisallowableAmount.error.overAllowableMax"
  override lazy val optionalArguments: Option[Seq[String]] = Some(Seq(amountString))
  override lazy val maximum: BigDecimal                    = amount

}
