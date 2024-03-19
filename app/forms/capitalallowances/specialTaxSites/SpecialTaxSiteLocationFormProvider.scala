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
import forms.mappings.Mappings
import models.common.UserType
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocation
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.Condition
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIf

import javax.inject.Inject

class SpecialTaxSiteLocationFormProvider @Inject() extends Mappings {
  private val buildingName              = "buildingName"
  private val buildingNumber            = "buildingNumber"
  private val postcode                  = "postcode"
  private val maxBuildingNameLength     = 50
  private val maxBuildingNameError      = "specialTaxSiteLocation.error.buildingName.length"
  private val maxBuildingNumberLength   = 10
  private val maxBuildingNumberError    = "specialTaxSiteLocation.error.buildingNumber.length"
  private val emptyBuildingDetailsError = (userType: UserType) => s"specialTaxSiteLocation.error.building.$userType"
  private val postcodeRequiredError     = (userType: UserType) => s"specialTaxSiteLocation.error.postcode.$userType"
  private val postcodeInvalidError      = "specialTaxSiteLocation.error.postcode.invalid"

  def isEmpty(field: String): Condition = _.get(field).exists(_.isEmpty)

  def apply(userType: UserType): Form[SpecialTaxSiteLocation] = Form(
    mapping(
      buildingName -> mandatoryIf(
        isEmpty(buildingNumber),
        text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxBuildingNameLength, maxBuildingNameError))),
      buildingNumber -> mandatoryIf(
        isEmpty(buildingName),
        text(emptyBuildingDetailsError(userType)).verifying(maxLength(maxBuildingNumberLength, maxBuildingNumberError))),
      postcode -> text(postcodeRequiredError(userType)).verifying(regexp(PostcodeRegex, postcodeInvalidError))
    )(SpecialTaxSiteLocation.apply)(SpecialTaxSiteLocation.unapply).verifying(
    )
  )
}
