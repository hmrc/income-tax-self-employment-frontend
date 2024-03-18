package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WdaMainRatePageSpec extends PageSpecBase(WdaMainRatePage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaMainRate" -> false), true),
    (Json.obj("wdaMainRate" -> true, "wdaMainRateClaimAmount" -> Some(10.0), "wdaSingleAsset" -> false), true)
  )
}
