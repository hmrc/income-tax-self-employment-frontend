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
import forms.capitalallowances.specialTaxSites.SpecialTaxSiteLocationFormProvider.{buildingName, buildingNumber, emptyBuildingDetailsError, postcode}
import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocation
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}
import uk.gov.voa.play.form.Condition
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIf

import javax.inject.Inject

class SpecialTaxSiteLocationFormProvider @Inject() extends Mappings {
  private val maxInputLength         = 90
  private val maxBuildingNameError   = "specialTaxSiteLocation.error.buildingName.length" // TODO get the messages for these from Tim
  private val maxBuildingNumberError = "specialTaxSiteLocation.error.buildingNumber.length"
  private val postcodeRequiredError  = (userType: UserType) => s"specialTaxSiteLocation.error.postcode.$userType"
  private val postcodeInvalidError   = "error.postcode.invalid"

  private def bindIfOneOrBothAreFilled(dependentField: String): Condition = { s =>
    val bothFull   = s.get(buildingName).exists(_.nonEmpty) && s.get(buildingNumber).exists(_.nonEmpty)
    val otherEmpty = s.get(dependentField).exists(_.isEmpty)
    otherEmpty || bothFull
  }

  def apply(userType: UserType): Form[SpecialTaxSiteLocation] = Form(
    mapping(
      buildingName -> mandatoryIf(
        bindIfOneOrBothAreFilled(buildingNumber),
        text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxInputLength, maxBuildingNameError))
      ),
      buildingNumber -> mandatoryIf(
        bindIfOneOrBothAreFilled(buildingName),
        text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxInputLength, maxBuildingNumberError))
      ),
      postcode -> text(postcodeRequiredError(userType), toUpperCase = true).verifying(regexp(PostcodeRegex, postcodeInvalidError))
    )(SpecialTaxSiteLocation.apply)(SpecialTaxSiteLocation.unapply))

}

object SpecialTaxSiteLocationFormProvider {
  val buildingName   = "buildingName"
  val buildingNumber = "buildingNumber"
  val postcode       = "postcode"
  val emptyBuildingDetailsError: UserType => String =
    (userType: UserType) => s"specialTaxSiteLocation.error.building.$userType"

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
