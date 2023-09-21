package models.viewModels

import models.requests.BusinessData
import play.api.libs.json.{Json, OFormat}

case class TaggedSelfEmploymentsViewModel(businessData: BusinessData,
                                          status: String)

object TaggedSelfEmploymentsViewModel {
  implicit val format: OFormat[TaggedSelfEmploymentsViewModel] = Json.format[TaggedSelfEmploymentsViewModel]
}
