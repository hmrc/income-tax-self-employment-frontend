package pages.nics

import base.SpecBase._
import models.common.BusinessId.nationalInsuranceContributions
import org.scalatest.wordspec.AnyWordSpecLike

class Class4NICsPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to CYA page when answer = false" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = false)
      val result  = Class4NICsPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url === s"/${taxYear}/national-insurance/check-your-answers")
    }

    "navigate to the Exemption page when answer = true" in {
      val answers = setBooleanAnswer(Class4NICsPage, nationalInsuranceContributions, answer = true)
      val result  = Class4NICsPage.nextPageInNormalMode(answers, nationalInsuranceContributions, taxYear)
      assert(result.url === s"/${taxYear}/national-insurance/class-4-exemption-category")
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
