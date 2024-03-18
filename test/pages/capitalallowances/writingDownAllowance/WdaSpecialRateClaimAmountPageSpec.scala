package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WdaSpecialRateClaimAmountPageSpec extends PageSpecBase(WdaSpecialRateClaimAmountPage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaSpecialRateClaimAmount" -> Some(10.0), "wdaMainRate" -> false), true)
  )
}
