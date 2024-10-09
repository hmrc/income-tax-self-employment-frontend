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

package pages.nics

import base.SpecBase._
import models.common.BusinessId
import models.common.BusinessId.nationalInsuranceContributions
import models.database.UserAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class Class4DivingExemptPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to CYA page on valid answer" in {
      val answers = emptyUserAnswers.set(Class4NonDivingExemptPage, List.empty[BusinessId], Some(nationalInsuranceContributions)).success.value
      val result  = Class4DivingExemptPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance-contributions/check-answers"))
    }

    "navigate to trustee executor administrator single business page if single business is unselected" in {

      val businesses = Json.obj(
        "Business1" -> Json.obj(
          "businessId"  -> "Business1",
          "tradingName" -> "Business One"
        ),
        "Business2" -> Json.obj(
          "businessId"  -> "Business2",
          "tradingName" -> "Business Two"
        )
      )

      val answers = UserAnswers("id", businesses)
        .set(Class4DivingExemptPage, List(BusinessId("Business1")), Some(nationalInsuranceContributions))
        .success
        .value

      val result = Class4DivingExemptPage.nextPageInNormalMode(answers, BusinessId.nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance-contributions/class-4-exemption-reasons/trustee-executor-administrator-single"))
    }

    "navigate to trustee executor administrator page if multiple businesses are unselected" in {

      val businesses = Json.obj(
        "Business1" -> Json.obj(
          "businessId"  -> "Business1",
          "tradingName" -> "Business One"
        ),
        "Business2" -> Json.obj(
          "businessId"  -> "Business2",
          "tradingName" -> "Business Two"
        ),
        "Business3" -> Json.obj(
          "businessId"  -> "Business3",
          "tradingName" -> "Business Three"
        )
      )

      val answers = UserAnswers("id", businesses)
        .set(Class4DivingExemptPage, List(BusinessId("Business1")), Some(nationalInsuranceContributions))
        .success
        .value

      val result = Class4DivingExemptPage.nextPageInNormalMode(answers, BusinessId.nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance-contributions/class-4-exemption-reasons/trustee-executor-administrator"))
    }
  }
}
