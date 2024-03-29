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

package forms.capitalallowances.specialTaxSites

import forms.PostcodeRegex
import forms.behaviours.StringFieldBehaviours
import models.common.UserType.Individual
import play.api.data.FormError

class SpecialTaxSiteLocationFormProviderSpec extends StringFieldBehaviours {
  private val buildingName              = "buildingName"
  private val buildingNumber            = "buildingNumber"
  private val postcode                  = "postcode"
  private val maxLength                 = 90
  private val emptyBuildingDetailsError = "specialTaxSiteLocation.error.building.individual"
  private val postcodeRequiredError     = "specialTaxSiteLocation.error.postcode.individual"
  private val postcodeInvalidError      = "error.postcode.invalid"
  private val validPostCode             = "GU84NB"

  val form = new SpecialTaxSiteLocationFormProvider()(Individual)

  "buildingName" - {

    behave like fieldThatBindsValidData(
      form,
      buildingName,
      stringsWithMaxLength(maxLength)
    )

  }

  "buildingNumber" - {

    behave like fieldThatBindsValidData(
      form,
      buildingNumber,
      stringsWithMaxLength(maxLength)
    )

  }

  "postcode" - {

    behave like fieldThatBindsValidData(
      form,
      postcode,
      validPostCode
    )

    behave like fieldThatOnlyBindsRegexValidData(
      form,
      postcode,
      PostcodeRegex,
      FormError(postcode, postcodeInvalidError, List(PostcodeRegex))
    )

    behave like mandatoryField(
      form,
      postcode,
      requiredError = FormError(postcode, postcodeRequiredError)
    )
  }

  "form should return errors when both buildingName and buildingNumber fields are empty" in {
    val result = form
      .bind(
        Map(
          buildingName   -> "",
          buildingNumber -> "",
          postcode       -> validPostCode
        ))
      .errors
      .toList
    result mustEqual List(FormError(buildingName, emptyBuildingDetailsError), FormError(buildingNumber, emptyBuildingDetailsError))
  }

}
