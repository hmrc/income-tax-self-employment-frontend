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

package models.journeys.nics

import models.common.BusinessId
import play.api.libs.json.{Format, Json}

case class NICsJourneyAnswers(class2NICs: Option[Boolean],
                              class4NICs: Option[Boolean],
                              class4ExemptionReason: Option[ExemptionReason],
                              class4DivingExempt: Option[List[BusinessId]],
                              class4NonDivingExempt: Option[List[BusinessId]])

object NICsJourneyAnswers {
  implicit val formats: Format[NICsJourneyAnswers] = Json.format[NICsJourneyAnswers]
}
