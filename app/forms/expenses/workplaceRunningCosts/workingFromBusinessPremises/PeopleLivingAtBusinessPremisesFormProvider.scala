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

package forms.expenses.workplaceRunningCosts.workingFromBusinessPremises

import forms.mappings.Mappings
import models.common.UserType
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages

object PeopleLivingAtBusinessPremisesFormProvider extends Mappings {

  case class PeopleLivingAtBusinessPremisesFormModel(onePerson: Int, twoPeople: Int, threePeople: Int)

  private val onePerson   = "onePerson"
  private val twoPeople   = "twoPeople"
  private val threePeople = "threePeople"

  def apply(userType: UserType, maxMonths: Int)(implicit messages: Messages): Form[PeopleLivingAtBusinessPremisesFormModel] = {
    val requiredError     = "peopleLivingAtBusinessPremises.error.required."
    val nonNumericError   = "expenses.error.nonNumeric"
    val noDecimalsError   = "expenses.error.noDecimals"
    val lessThanZeroError = "expenses.error.lessThanZero"
    val overMaxError      = "peopleLivingAtBusinessPremises.error.overMax."
    val totalOverMaxError = "peopleLivingAtBusinessPremises.error.overMax.total."

    def validateNumberOfPeople(valueKey: String): Mapping[Int] =
      int(messages(s"$requiredError$valueKey", maxMonths), messages(noDecimalsError, maxMonths), messages(nonNumericError, maxMonths))
        .verifying(minimumValue(0, lessThanZeroError))
        .verifying(lessThanOrEqualTo(maxMonths, messages(s"$overMaxError$valueKey.$userType", maxMonths), Some(maxMonths.toString)))

    Form[PeopleLivingAtBusinessPremisesFormModel](
      mapping(
        onePerson   -> validateNumberOfPeople(onePerson),
        twoPeople   -> validateNumberOfPeople(twoPeople),
        threePeople -> validateNumberOfPeople(threePeople)
      )(PeopleLivingAtBusinessPremisesFormModel.apply)(PeopleLivingAtBusinessPremisesFormModel.unapply)
        .verifying(messages(s"$totalOverMaxError$userType", maxMonths), form => form.onePerson + form.twoPeople + form.threePeople <= maxMonths)
    )
  }

}
