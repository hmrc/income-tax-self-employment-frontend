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

package forms.capitalallowances.structuresBuildingsAllowance

import models.common.UserType
import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}

class StructuresBuildingsNewClaimAmountFormProviderSpec extends PlaySpec {

  val formProvider: StructuresBuildingsNewClaimAmountFormProvider = new StructuresBuildingsNewClaimAmountFormProvider()
  val formIndividual: Form[BigDecimal]                            = formProvider(UserType.Individual)
  val formAgent: Form[BigDecimal]                                 = formProvider(UserType.Agent)

  "StructuresBuildingsNewClaimAmountFormProvider" must {

    "bind valid data for Individual" in {
      val result = formIndividual.bind(Map("value" -> "1000.00"))
      result.errors mustBe empty

      result.get mustBe BigDecimal(1000.00)
    }

    "bind valid data for Agent" in {
      val result = formAgent.bind(Map("value" -> "5000.00"))
      result.errors mustBe empty

      result.get mustBe BigDecimal(5000.00)
    }

    "fail to bind when value is missing for Individual" in {
      val result = formIndividual.bind(Map.empty[String, String])

      result.errors must contain(FormError("value", "structuresBuildingsNewClaimAmount.error.required.individual"))
    }

    "fail to bind when value is missing for Agent" in {
      val result = formAgent.bind(Map.empty[String, String])

      result.errors must contain(FormError("value", "structuresBuildingsNewClaimAmount.error.required.agent"))
    }

    "fail to bind when value is non-numeric for Individual" in {
      val result = formIndividual.bind(Map("value" -> "abc"))

      result.errors must contain(FormError("value", "error.nonNumeric.individual"))
    }

    "fail to bind when value is non-numeric for Agent" in {
      val result = formAgent.bind(Map("value" -> "abc"))

      result.errors must contain(FormError("value", "error.nonNumeric.agent"))
    }

    "fail to bind when value is less than minimum for Individual" in {
      val result = formIndividual.bind(Map("value" -> "-1"))

      result.errors must contain(FormError("value", "error.lessThanZero", Seq(BigDecimal(0))))
    }

    "fail to bind when value is less than minimum for Agent" in {
      val result = formAgent.bind(Map("value" -> "-1"))

      result.errors must contain(FormError("value", "error.lessThanZero", Seq(BigDecimal(0))))
    }

    "fail to bind when value exceeds maximum for Individual" in {
      val result = formIndividual.bind(Map("value" -> "100000000000.01"))

      result.errors must contain(FormError("value", "error.overMax", Seq(BigDecimal(100000000000.00))))
    }

    "fail to bind when value exceeds maximum for Agent" in {
      val result = formAgent.bind(Map("value" -> "100000000000.01"))

      result.errors must contain(FormError("value", "error.overMax", Seq(BigDecimal(100000000000.00))))
    }
  }
}
