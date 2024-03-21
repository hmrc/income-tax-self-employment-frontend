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

package forms.capitalallowances.specialTaxSites

import forms.PostcodeRegex
import forms.capitalallowances.specialTaxSites.SpecialTaxSiteLocationFormProvider.{buildingName, buildingNumber, emptyBuildingDetailsError}
import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocation
import play.api.data.Forms.{mapping, optional}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{Form, FormError}

import javax.inject.Inject

class SpecialTaxSiteLocationFormProvider @Inject() extends Mappings {
  private val postcode                = "postcode"
  private val maxBuildingNameLength   = 100
  private val maxBuildingNameError    = "specialTaxSiteLocation.error.buildingName.length"
  private val maxBuildingNumberLength = 20
  private val maxBuildingNumberError  = "specialTaxSiteLocation.error.buildingNumber.length"
  private val postcodeRequiredError   = (userType: UserType) => s"specialTaxSiteLocation.error.postcode.$userType"
  private val postcodeInvalidError    = "error.postcode.invalid"

  private def atLeastOneRequired(userType: UserType): Constraint[SpecialTaxSiteLocation] = Constraint("constraints.atleastone") { location =>
    if (location.buildingName.isEmpty && location.buildingNumber.isEmpty) {
      Invalid(Seq(ValidationError(emptyBuildingDetailsError(userType))))
    } else {
      Valid
    }
  }

  def apply(userType: UserType): Form[SpecialTaxSiteLocation] = Form(
    mapping(
      buildingName   -> optional(text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxBuildingNameLength, maxBuildingNameError))),
      buildingNumber -> optional(text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxBuildingNumberLength, maxBuildingNumberError))),
      postcode       -> text(postcodeRequiredError(userType), toUpperCase = true).verifying(regexp(PostcodeRegex, postcodeInvalidError))
    )(SpecialTaxSiteLocation.apply)(SpecialTaxSiteLocation.unapply).verifying(atLeastOneRequired(userType))
  )
}

object SpecialTaxSiteLocationFormProvider {
  val buildingName              = "buildingName"
  val buildingNumber            = "buildingNumber"
  val emptyBuildingDetailsError = (userType: UserType) => s"specialTaxSiteLocation.error.building.$userType"

  def filterErrors(form: Form[SpecialTaxSiteLocation], userType: UserType): Form[SpecialTaxSiteLocation] = {
    val formError = FormError("", List(emptyBuildingDetailsError(userType)), List())
    if (form.errors.contains(formError))
      form
        .copy(errors = form.errors.filterNot(_.equals(formError)))
        .withError(formError.copy(key = buildingName))
        .withError(formError.copy(key = buildingNumber))
    else form
  }
}
