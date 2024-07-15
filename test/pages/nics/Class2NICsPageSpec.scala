package pages.nics

import base.SpecBase._
import models.common.BusinessId.nationalInsuranceContributions
import org.scalatest.wordspec.AnyWordSpecLike

class Class2NICsPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to CYA page" in {
      val result = Class2NICsPage.nextPageInNormalMode(emptyUserAnswers, nationalInsuranceContributions, taxYear)
      assert(result.url === s"/${taxYear}/national-insurance/check-your-answers")
    }
  }

  "hasAllFurtherAnswers" should {
    "return false if no answers" in {
      val result = Class2NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, emptyUserAnswers)
      assert(result === false)
    }

    "return true if answer = true" in {
      val answers = setBooleanAnswer(Class2NICsPage, nationalInsuranceContributions, answer = true)
      val result  = Class2NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }

    "return true if answer = false" in {
      val answers = setBooleanAnswer(Class2NICsPage, nationalInsuranceContributions, answer = false)
      val result  = Class2NICsPage.hasAllFurtherAnswers(nationalInsuranceContributions, answers)
      assert(result === true)
    }
  }

}
