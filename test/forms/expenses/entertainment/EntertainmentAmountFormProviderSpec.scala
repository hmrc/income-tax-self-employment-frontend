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

package forms.expenses.entertainment

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import play.api.data.Form

class EntertainmentAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("EntertainmentAmountFormProvider") {

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new EntertainmentAmountFormProvider()(userType)

  override lazy val requiredError: String     = "entertainment.error.required"
  override lazy val nonNumericError: String   = "entertainment.error.nonNumeric"
  override lazy val lessThanZeroError: String = "entertainment.error.lessThanZero"
  override lazy val overMaxError: String      = "entertainment.error.overMax"

}
