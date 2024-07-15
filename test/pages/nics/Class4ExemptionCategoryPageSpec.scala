package pages.nics

import base.SpecBase._
import models.common.BusinessId.nationalInsuranceContributions
import models.journeys.nics.ExemptionCategory._
import org.scalatest.wordspec.AnyWordSpecLike

class Class4ExemptionCategoryPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to there ia a problem if no answer" in {
      val result = Class4ExemptionCategoryPage.nextPageInNormalMode(emptyUserAnswers, nationalInsuranceContributions, taxYear)
      assert(result.url === "/there-is-a-problem")
    }

    "navigate to CYA page for single business when TrusteeExecutorAdmin" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, TrusteeExecutorAdmin, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url === s"/$taxYear/national-insurance/class-4-non-diving-exempt")
    }

    "navigate to CYA page for single business when DiverDivingInstructor" in {
      val answers = emptyUserAnswers.set(Class4ExemptionCategoryPage, DiverDivingInstructor, Some(nationalInsuranceContributions)).success.value
      val result  = Class4ExemptionCategoryPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url === s"/$taxYear/national-insurance/class-4-diving-exempt")
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
