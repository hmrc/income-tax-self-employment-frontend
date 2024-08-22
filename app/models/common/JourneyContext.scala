/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.common

import models.common.Journey
import models.requests.{DataRequest, NinoDataRequest}
import play.api.mvc.AnyContent

sealed trait JourneyContext {
  val taxYear: TaxYear
  val businessId: BusinessId
  val mtditid: Mtditid
  val nino: Nino
  val journey: Journey

  val answersUrl: String
}

case class JourneyContextWithNino(
    taxYear: TaxYear,
    nino: Nino,
    businessId: BusinessId,
    mtditid: Mtditid,
    journey: Journey,
    extraContext: Option[String] = None
) extends JourneyContext {
  val answersUrl: String = {
    val optExtraContext: String = if (extraContext.isEmpty) "" else s"/${extraContext.getOrElse("")}"
    s"${taxYear.endYear}/${businessId.value}/${journey.toString}$optExtraContext/${nino.value}/answers"
  }
}

object JourneyContextWithNino {
  def apply(taxYear: TaxYear, businessId: BusinessId, journey: Journey)(implicit request: DataRequest[AnyContent]) =
    new JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, journey)
}

// TODO Now every context must have nino for Audit purpose. Merge our two contexts.
case class JourneyAnswersContext(taxYear: TaxYear,
                                 nino: Nino,
                                 businessId: BusinessId,
                                 mtditid: Mtditid,
                                 journey: Journey,
                                 extraContext: Option[String] = None)
    extends JourneyContext {
  val answersUrl: String = {
    val optExtraContext: String = if (extraContext.isEmpty) "" else s"/${extraContext.getOrElse("")}"
    s"${taxYear.endYear}/${businessId.value}/${journey.toString}$optExtraContext/answers"
  }
}

object JourneyAnswersContext {
  def fromNinoDataRequest(taxYear: TaxYear,
                          businessId: BusinessId,
                          request: NinoDataRequest,
                          journey: Journey,
                          extraContext: Option[String] = None): JourneyAnswersContext =
    JourneyAnswersContext(taxYear, request.nino, businessId, request.mtditid, journey, extraContext)

}
