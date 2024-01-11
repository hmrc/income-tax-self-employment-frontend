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

package forms.expenses.tailoring

import base.forms.CurrencyFormProviderBaseSpec
import forms.expenses.construction.ConstructionIndustryAmountFormProvider
import models.common.UserType
import play.api.data.Form

class ConstructionIndustryAmountFormProviderSpec extends CurrencyFormProviderBaseSpec("ConstructionIndustryAmountFormProvider") {

  override def getFormProvider(userType: UserType): Form[BigDecimal] = new ConstructionIndustryAmountFormProvider()(userType)

  override lazy val requiredError: String     = "constructionIndustryAmount.error.required"
  override lazy val nonNumericError: String   = "constructionIndustryAmount.error.nonNumeric"
  override lazy val lessThanZeroError: String = "constructionIndustryAmount.error.lessThanZero"
  override lazy val overMaxError: String      = "constructionIndustryAmount.error.overMax"

}
