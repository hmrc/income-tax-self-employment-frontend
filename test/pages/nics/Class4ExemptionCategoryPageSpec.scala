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
import models.journeys.nics.ExemptionCategory._
import org.scalatest.wordspec.AnyWordSpecLike

class Class4ExemptionCategoryPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to there ia a problem if no answer" in {
      val result = Class4ExemptionCategoryPage.nextPageInNormalMode(emptyUserAnswers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith("/there-is-a-problem"))
    }

    "navigate to CYA page for single business when TrusteeExecutorAdmin" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, TrusteeExecutorAdmin, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance/class-4-non-diving-exempt"))
    }

    "navigate to CYA page for single business when DiverDivingInstructor" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, DiverDivingInstructor, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance/class-4-diving-exempt"))
    }
  }

  "hasAllFurtherAnswers" should {
    "return false if no answers" in {
      val result = Class4ExemptionCategoryPage.hasAllFurtherAnswers(nationalInsuranceContributions, emptyUserAnswers)
      assert(result === false)
    }

    "return true if answer = TrusteeExecutorAdmin" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, TrusteeExecutorAdmin, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }

    "return true if answer = DiverDivingInstructor" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, DiverDivingInstructor, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }
  }

}
