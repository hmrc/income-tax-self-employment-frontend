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

package models.audit

import models.common.{Mtditid, TaxYear}
import models.journeys.Journey
import play.api.libs.json.{JsObject, Json, OWrites, Writes}

sealed trait SelfEmploymentAuditEvent

final case class SelfEmploymentCYAnswersAuditEvent(
    mtditid: Mtditid,
    taxYear: TaxYear,
    sectionName: SectionName,
    journeyName: Journey,
    priorAnswers: Option[JsObject],
    submittedAnswers: JsObject
) extends SelfEmploymentAuditEvent

object SelfEmploymentCYAnswersAuditEvent {
  implicit val writes: OWrites[SelfEmploymentCYAnswersAuditEvent] = Json.writes[SelfEmploymentCYAnswersAuditEvent]
}

object SelfEmploymentAuditEvent {
  implicit def writes: Writes[SelfEmploymentAuditEvent] = { case e: SelfEmploymentCYAnswersAuditEvent =>
    Json.toJson(e)(SelfEmploymentCYAnswersAuditEvent.writes)
  }
}
