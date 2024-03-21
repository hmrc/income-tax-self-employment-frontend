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

package forms.standard

import base.forms.CurrencyFormProviderBaseSpec
import models.common.UserType
import pages.OneQuestionPage
import play.api.data.Form

class CurrencyFormProviderSpec extends CurrencyFormProviderBaseSpec("CurrencyFormProviderSpec") {
  object TestPage extends OneQuestionPage[BigDecimal] {
    override def toString: String = "testPage"
  }

  override lazy val requiredError: String = "testPage.error.required"

  override def nonNumericErrorNoUserType: Option[String] = Some("error.nonNumeric")
  def nonNumericError: String                            = "not used"

  override def lessThanZeroError: String                   = "not used"
  override def lessThanZeroErrorNoUserType: Option[String] = Some("error.lessThanZero")

  override def overMaxError: String                   = "not used"
  override def overMaxErrorNoUserType: Option[String] = Some("error.overMax")

  def getFormProvider(userType: UserType): Form[BigDecimal] = new CurrencyFormProvider()(TestPage, userType)

}
