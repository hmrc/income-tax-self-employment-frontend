/*
 * Copyright 2024 HM Revenue & Customs
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

import models.journeys.Journey

sealed trait JourneyContext {
  val taxYear: TaxYear
  val businessId: BusinessId
  val mtditid: Mtditid
  val journey: Journey

  val answersUrl: String
}

case class JourneyContextWithNino(taxYear: TaxYear,
                                  nino: Nino,
                                  businessId: BusinessId,
                                  mtditid: Mtditid,
                                  journey: Journey,
                                  extraContext: Option[String] = None)
    extends JourneyContext {
  val answersUrl: String = {
    val optExtraContext: String = if (extraContext.isEmpty) "" else s"/${extraContext.getOrElse("")}"
    s"${taxYear.value}/${businessId.value}/${journey.toString}$optExtraContext/${nino.value}/answers"
  }
}

case class JourneyAnswersContext(taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, journey: Journey, extraContext: Option[String] = None)
    extends JourneyContext {
  val answersUrl: String = {
    val optExtraContext: String = if (extraContext.isEmpty) "" else s"/${extraContext.getOrElse("")}"
    s"${taxYear.value}/${businessId.value}/${journey.toString}$optExtraContext/answers"
  }
}
