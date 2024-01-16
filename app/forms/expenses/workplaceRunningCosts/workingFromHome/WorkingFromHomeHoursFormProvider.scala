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

package forms.expenses.workplaceRunningCosts.workingFromHome

import forms.mappings.Mappings
import models.common.UserType
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object WorkingFromHomeHoursFormProvider extends Mappings {

  case class WorkingFromHomeHoursFormModel(value25To50: Int, value51To100: Int, value101Plus: Int)

  private val value25To50  = "value25To50"
  private val value51To100 = "value51To100"
  private val value101Plus = "value101Plus"

  def apply(userType: UserType, maxMonths: Int)(implicit messages: Messages): Form[WorkingFromHomeHoursFormModel] = {
    val requiredError     = "workingFromHomeHours.error.required."
    val nonNumericError   = "workingFromHomeHours.error.nonNumeric"
    val noDecimalsError   = "workingFromHomeHours.error.noDecimals"
    val lessThanZeroError = "workingFromHomeHours.error.lessThanZero"
    val overMaxError      = "workingFromHomeHours.error.overMax."
    val totalOverMaxError = "workingFromHomeHours.error.overMax.total."

    def validateHours(valueKey: String): Mapping[Int] =
      int(s"$requiredError$valueKey", noDecimalsError, nonNumericError)
        .verifying(minimumValue(0, lessThanZeroError))
        .verifying(lessThanOrEqualTo(maxMonths, s"$overMaxError$valueKey.$userType", Some(maxMonths.toString)))

    Form[WorkingFromHomeHoursFormModel](
      mapping(
        value25To50  -> validateHours(value25To50),
        value51To100 -> validateHours(value51To100),
        value101Plus -> validateHours(value101Plus)
      )(WorkingFromHomeHoursFormModel.apply)(WorkingFromHomeHoursFormModel.unapply)
        .verifying(messages(s"$totalOverMaxError$userType", maxMonths), form => form.value25To50 + form.value51To100 + form.value101Plus <= maxMonths)
    )
  }

}
