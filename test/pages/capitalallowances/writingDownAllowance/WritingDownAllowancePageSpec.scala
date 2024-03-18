package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WritingDownAllowancePageSpec extends PageSpecBase(WritingDownAllowancePage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaSpecialRate" -> false), true)
  )
}
