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

package services

import models.audit.{AuditEventType, AuditSectionName, CYAnswersAuditEvent}
import models.common.Journey.NationalInsuranceContributions
import models.common.{Journey, JourneyContext}
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait AuditService {
  def unsafeSendExplicitCYAAuditEvent(context: JourneyContext, answers: JsObject, wasSuccessful: Boolean)(implicit hc: HeaderCarrier): Unit
}

@Singleton
class AuditServiceImpl @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) extends AuditService with Logging {
  def unsafeSendExplicitCYAAuditEvent(context: JourneyContext, answers: JsObject, wasSuccessful: Boolean)(implicit hc: HeaderCarrier): Unit = {
    val auditEvent = CYAnswersAuditEvent(
      context.mtditid,
      context.nino,
      context.taxYear,
      toAuditSectionName(context.journey),
      context.journey,
      None, // TODO do we want previous answers from the backend?
      answers,
      wasSuccessful
    )

    context.journey match {
      case Journey.NationalInsuranceContributions =>
        auditConnector.sendExplicitAudit(AuditEventType.CreateOrUpdateNationalInsuranceContributionsAuditType.entryName, auditEvent)
      case _ =>
        () // TODO do nothing for now, we'll get back to it once we know more business requirements
    }
  }

  private def toAuditSectionName(journey: Journey): Option[AuditSectionName] =
    journey match {
      case NationalInsuranceContributions => Some(AuditSectionName.NationalInsuranceContributionsAuditSection)
      case _                              => None
    }

}
