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
import models.common.BusinessId.nationalInsuranceContributions
import org.scalatest.wordspec.AnyWordSpecLike

class Class4NICsPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to CYA page when answer = false" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = false)
      val result  = Class4NICsPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/${taxYear}/national-insurance/check-your-answers"))
    }

    "navigate to the Exemption page when answer = true" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = true)
      val result  = Class4NICsPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/${taxYear}/national-insurance/class-4-exemption-category"))
    }
  }

  "hasAllFurtherAnswers" should {
    "return false if no answers" in {
      val result = Class4NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, emptyUserAnswers)
      assert(result === false)
    }

    "return true if answer = true" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = true)
      val result  = Class4NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }

    "return true if answer = false" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = false)
      val result  = Class4NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }
  }

}
