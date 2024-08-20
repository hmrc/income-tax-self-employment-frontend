package models.frontend.nics

import models.common.BusinessId
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
import play.api.libs.json.{Format, Json}

case class NICsClass4Answers(class4NICs: Boolean,
                             class4ExemptionReason: Option[ExemptionReason],
                             class4DivingExempt: Option[List[BusinessId]],
                             class4NonDivingExempt: Option[List[BusinessId]]) {

  def userHasSingleBusiness: Boolean = class4ExemptionReason.isDefined

  def toMultipleBusinessesAnswers: List[Class4ExemptionAnswers] = {
    val divers =
      class4DivingExempt.fold(List.empty[Class4ExemptionAnswers])(_.map(id => Class4ExemptionAnswers(id, ExemptionReason.DiverDivingInstructor)))
    val trustees =
      class4NonDivingExempt.fold(List.empty[Class4ExemptionAnswers])(_.map(id => Class4ExemptionAnswers(id, ExemptionReason.TrusteeExecutorAdmin)))
    divers ++ trustees
  }

}

object NICsClass4Answers {
  implicit val formats: Format[NICsClass4Answers] = Json.format[NICsClass4Answers]

  case class Class4ExemptionAnswers(businessId: BusinessId, exemptionReason: ExemptionReason)
}
