package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WdaSingleAssetPageSpec extends PageSpecBase(WdaSingleAssetPage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaSingleAsset" -> false), true),
    (Json.obj("wdaSingleAsset" -> true, "wdaSingleAssetClaimAmounts" -> Some(10.0)), true)
  )
}
