package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WdaSpecialRatePageSpec extends PageSpecBase(WdaSpecialRatePage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaSpecialRate" -> false), true),
    (Json.obj("wdaSpecialRate" -> true, "wdaSpecialRateClaimAmount" -> Some(10.0), "wdaMainRate" -> false), true)
  )
}
