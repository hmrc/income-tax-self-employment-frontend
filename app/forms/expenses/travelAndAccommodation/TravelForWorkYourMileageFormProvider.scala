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

package forms.expenses.travelAndAccommodation

import forms.mappings.Mappings
import models.common.{MoneyBounds, UserType}
import play.api.data.Form

import javax.inject.Inject

class TravelForWorkYourMileageFormProvider @Inject() extends Mappings with MoneyBounds {

  def apply(userType: UserType, vehicle: String): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal(
        s"travelForWorkYourMileage.error.required.$userType",
        s"travelForWorkYourMileage.error.nonNumeric.$userType",
        args = Seq(vehicle)
      )
        .verifying(greaterThan(minimumValue, s"travelForWorkYourMileage.error.lessThanZero.$userType", Some(vehicle)))
        .verifying(lessThan(maximumValue, s"travelForWorkYourMileage.error.overMax.$userType", Some(vehicle)))
    )
}
