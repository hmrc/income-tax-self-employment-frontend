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

package pages.nics

import controllers.journeys.nics.routes
import controllers.standard
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.nics.ExemptionReason
import models.journeys.nics.ExemptionReason.{DiverDivingInstructor, TrusteeExecutorAdmin}
import play.api.mvc.Call
import queries.Settable

import scala.collection.immutable._

case object Class4ExemptionReasonPage extends NicsBasePage[ExemptionReason] {
  override def toString: String = "class4ExemptionReason"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    userAnswers.get(this, businessId) match {
      case Some(TrusteeExecutorAdmin)  => routes.Class4NonDivingExemptController.onPageLoad(taxYear, NormalMode)
      case Some(DiverDivingInstructor) => routes.Class4DivingExemptController.onPageLoad(taxYear, NormalMode)
      case None                        => standard.routes.JourneyRecoveryController.onPageLoad()
    }

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined

  override val dependentPagesWhenAnswerChanges: List[Settable[_]] =
    List(Class4NonDivingExemptPage, Class4DivingExemptPage)
}
