/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import forms.mappings.Mappings
import models.common.UserType
import play.api.data.Form

import javax.inject.Inject

class TravelForWorkYourMileageFormProvider @Inject() extends Mappings {

  def apply(userType: UserType, vehicle: String): Form[Int] =
    Form(
      "value" -> int(
        s"travelForWorkYourMileage.error.required.$userType",
        s"travelForWorkYourMileage.error.wholeNumber.$userType",
        s"travelForWorkYourMileage.error.nonNumeric.$userType"
      )
        .verifying(inRange(0, Int.MaxValue, s"travelForWorkYourMileage.error.outOfRange.$userType"))
    )
}
