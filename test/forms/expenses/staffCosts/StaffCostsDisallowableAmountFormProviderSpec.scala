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

package forms.expenses.staffCosts

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import org.scalacheck.Gen
import play.api.data.Form
import utils.MoneyUtils.formatMoney

class StaffCostsDisallowableAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("StaffCostsDisallowableAmountFormProvider") {

  private lazy val staffCosts       = 5623.50
  private lazy val staffCostsString = formatMoney(staffCosts)

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new StaffCostsDisallowableAmountFormProvider()(userType, staffCosts)

  override lazy val requiredError: String                  = "staffCostsDisallowableAmount.error.required"
  override lazy val nonNumericError: String                = "staffCostsDisallowableAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String              = "staffCostsDisallowableAmount.error.lessThanZero"
  override lazy val overMaxError: String                   = "staffCostsDisallowableAmount.error.overMax"
  override lazy val optionalArguments: Option[Seq[String]] = Some(Seq(staffCostsString))
  override lazy val validDataGenerator: Gen[String]        = currencyInRangeWithCommas(minimum, staffCosts)
}
