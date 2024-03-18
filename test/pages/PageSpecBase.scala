package pages

import base.SpecBase
import base.SpecBase.buildUserAnswers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}

abstract class PageSpecBase(page: OneQuestionPage[_]) extends TableDrivenPropertyChecks with AnyWordSpecLike {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean]

  "hasAllFurtherAnswers" should {
    "return correct value" in forAll(hasAllFurtherAnswersCases) { case (userAnswers, expected) =>
      assert(page.hasAllFurtherAnswers(SpecBase.businessId, buildUserAnswers(userAnswers)) === expected)
    }
  }
}
