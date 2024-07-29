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
import models.journeys.nics.ExemptionReason._
import org.scalatest.wordspec.AnyWordSpecLike

class Class4ExemptionReasonPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to there ia a problem if no answer" in {
      val result = Class4ExemptionReasonPage.nextPageInNormalMode(emptyUserAnswers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith("/there-is-a-problem"))
    }

    "navigate to CYA page for single business when DiverDivingInstructor" in {
      val answers = emptyUserAnswers.set(Class4ExemptionReasonPage, DiverDivingInstructor, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionReasonPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance-contributions/class-4-exemption-reasons/diver-diving-supervisor"))
    }

    "navigate to CYA page for single business when TrusteeExecutorAdmin" in {
      val answers = emptyUserAnswers.set(Class4ExemptionReasonPage, TrusteeExecutorAdmin, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionReasonPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url.endsWith(s"/$taxYear/national-insurance-contributions/class-4-exemption-reasons/trustee-executor-administrator"))
    }
  }

  "hasAllFurtherAnswers" should {
    "return false if no answers" in {
      val result = Class4ExemptionReasonPage.hasAllFurtherAnswers(nationalInsuranceContributions, emptyUserAnswers)
      assert(result === false)
    }

    "return true if answer = TrusteeExecutorAdmin and Class4NonDivingExemptPage has all further answers" in {
      val answers = emptyUserAnswers
        .set(Class4ExemptionReasonPage, TrusteeExecutorAdmin, Some(nationalInsuranceContributions))
        .success
        .value
        .set(Class4NonDivingExemptPage, List(businessId), Some(nationalInsuranceContributions))
        .success
        .value
      val result = Class4ExemptionReasonPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }

    "return true if answer = DiverDivingInstructor and Class4DivingExemptPage has all further answers" in {
      val answers = emptyUserAnswers
        .set(Class4ExemptionReasonPage, DiverDivingInstructor, Some(nationalInsuranceContributions))
        .success
        .value
        .set(Class4NonDivingExemptPage, List(businessId), Some(nationalInsuranceContributions))
        .success
        .value
        .set(Class4DivingExemptPage, List(businessId), Some(nationalInsuranceContributions))
        .success
        .value
      val result = Class4ExemptionReasonPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }
  }

}
