package pages.$journeyName;format="normalize,lower"$.$packageName$

import pages.OneQuestionPage
import play.api.libs.json.JsPath

case object $className$Page extends OneQuestionPage[String] {

  override def toString: String = "$className;format="decap"$"

}
