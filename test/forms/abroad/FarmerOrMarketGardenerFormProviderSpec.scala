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

package forms.abroad

import forms.behaviours.BooleanFieldBehaviours
import forms.industrysectors.FarmerOrMarketGardenerFormProvider
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.{Form, FormError}

class FarmerOrMarketGardenerFormProviderSpec extends BooleanFieldBehaviours {

  private val formProvider = new FarmerOrMarketGardenerFormProvider()

  private def form(userType: UserType): Form[Boolean] = formProvider(userType)

  ".apply" - {

    "for an Individual user" - {
      val userType = Individual
      val testForm = form(userType)

      "should bind true correctly" in {
        testForm.bind(Map("value" -> "true")).value mustBe Some(true)
      }

      "should bind false correctly" in {
        testForm.bind(Map("value" -> "false")).value mustBe Some(false)
      }

      "should return the correct error for empty value" in {
        val result = testForm.bind(Map("value" -> ""))
        result.errors must contain only FormError("value", s"farmerOrMarketGardener.error.required.$userType")
      }

      "should return the correct error for invalid value" in {
        val result = testForm.bind(Map("value" -> "invalid"))
        result.errors must contain only FormError("value", "error.boolean")
      }
    }

    "for an Agent user" - {
      val userType = Agent
      val testForm = form(userType)

      "should bind true correctly" in {
        testForm.bind(Map("value" -> "true")).value mustBe Some(true)
      }

      "should bind false correctly" in {
        testForm.bind(Map("value" -> "false")).value mustBe Some(false)
      }

      "should return the correct error for empty value" in {
        val result = testForm.bind(Map("value" -> ""))
        result.errors must contain only FormError("value", s"farmerOrMarketGardener.error.required.$userType")
      }

      "should return the correct error for invalid value" in {
        val result = testForm.bind(Map("value" -> "invalid"))
        result.errors must contain only FormError("value", "error.boolean")
      }
    }
  }

  ".value" - {
    val fieldName = "value"

    Seq(Individual, Agent).foreach { userType =>
      s"for $userType" - {
        behave like booleanField(
          form(userType),
          fieldName,
          invalidError = FormError(fieldName, "error.boolean")
        )

        behave like mandatoryField(
          form(userType),
          fieldName,
          requiredError = FormError(fieldName, s"farmerOrMarketGardener.error.required.$userType")
        )
      }
    }
  }
}
