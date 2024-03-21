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

package forms.capitalallowances.structuresBuildingsAllowance

import forms.PostcodeRegex
import forms.behaviours.StringFieldBehaviours
import models.common.UserType.Individual
import play.api.data.FormError
class StructuresBuildingsLocationFormProviderSpec extends StringFieldBehaviours {
  private val buildingName              = "buildingName"
  private val buildingNumber            = "buildingNumber"
  private val postcode                  = "postcode"
  private val maxBuildingNameLength     = 90
  private val maxBuildingNameError      = "structuresBuildingsLocation.error.buildingName.length"
  private val maxBuildingNumberLength   = 90
  private val maxBuildingNumberError    = "structuresBuildingsLocation.error.buildingNumber.length"
  private val emptyBuildingDetailsError = "structuresBuildingsLocation.error.building.individual"
  private val postcodeRequiredError     = "structuresBuildingsLocation.error.postcode.individual"
  private val postcodeInvalidError      = "error.postcode.invalid"
  private val validPostCode             = "GU84NB"

  val form = new StructuresBuildingsLocationFormProvider()(Individual)

  "buildingName" - {

    behave like fieldThatBindsValidData(
      form,
      buildingName,
      stringsWithMaxLength(maxBuildingNameLength)
    )

    behave like fieldWithMaxLength(
      form,
      buildingName,
      maxLength = maxBuildingNameLength,
      lengthError = FormError(buildingName, maxBuildingNameError, Seq(maxBuildingNameLength))
    )
  }

  "buildingNumber" - {

    behave like fieldThatBindsValidData(
      form,
      buildingNumber,
      stringsWithMaxLength(maxBuildingNumberLength)
    )

    behave like fieldWithMaxLength(
      form,
      buildingNumber,
      maxLength = maxBuildingNumberLength,
      lengthError = FormError(buildingNumber, maxBuildingNumberError, Seq(maxBuildingNumberLength))
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

  "form should return an error when both buildingName and buildingNumber fields are empty" in {
    val result = form
      .bind(
        Map(
          buildingName   -> "",
          buildingNumber -> "",
          postcode       -> validPostCode
        ))
      .errors
      .toList
    result mustEqual List(FormError("", emptyBuildingDetailsError))
  }

}
