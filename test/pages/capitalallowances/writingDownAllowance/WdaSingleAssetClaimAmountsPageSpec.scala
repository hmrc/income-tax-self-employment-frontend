package pages.capitalallowances.writingDownAllowance

import org.scalatest.prop.TableFor2
import pages.PageSpecBase
import play.api.libs.json.{JsObject, Json}

class WdaSingleAssetClaimAmountsPageSpec extends PageSpecBase(WdaSingleAssetClaimAmountsPage) {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean] = Table(
    ("userAnswers", "expected"),
    (Json.obj(), false),
    (Json.obj("wdaSingleAssetClaimAmounts" -> Some(10.0)), true)
  )
}
