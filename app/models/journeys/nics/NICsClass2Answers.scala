package models.frontend.nics

import play.api.libs.json.{Format, Json}

case class NICsClass2Answers (class2NICs: Boolean) extends NICsJourneyAnswers

object NICsClass2Answers {
  implicit val formats: Format[NICsClass2Answers] = Json.format[NICsClass2Answers]
}
