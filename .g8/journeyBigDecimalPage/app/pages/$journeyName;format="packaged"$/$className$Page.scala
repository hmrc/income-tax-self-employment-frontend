package pages.$journeyName$

import pages.QuestionPage
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[BigDecimal] {
  
  override def path(businessId: Option[String] = None): JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
