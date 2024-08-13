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
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import play.api.mvc.Call
import queries.Settable

case object Class4DivingExemptPage extends NicsBasePage[List[BusinessId]] {
  override def toString: String = "class4DivingExempt"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    if (Class4NonDivingExemptPage.remainingBusinesses(userAnswers).isEmpty)
      routes.NICsCYAController.onPageLoad(taxYear)
    else routes.Class4NonDivingExemptController.onPageLoad(taxYear, NormalMode)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined && Class4NonDivingExemptPage.hasAllFurtherAnswers(businessId, userAnswers)

  override val dependentPagesWhenAnswerChanges: List[Settable[_]] = List(Class4NonDivingExemptPage)
}
