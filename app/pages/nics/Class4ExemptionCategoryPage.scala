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
import models.journeys.nics.ExemptionCategory
import models.journeys.nics.ExemptionCategory.TrusteeExecutorAdmin
import play.api.mvc.Call
import queries.Settable

case object Class4ExemptionCategoryPage extends NicsBasePage[Set[ExemptionCategory]] {
  override def toString: String = "class4ExemptionCategory"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = redirectForExemptionCategory(
    userAnswers,
    TrusteeExecutorAdmin,
    routes.Class4NonDivingExemptController.onPageLoad(taxYear, NormalMode),
    routes.Class4NonDivingExemptController
      .onPageLoad(taxYear, NormalMode) // TODO to be changed in https://jira.tools.tax.service.gov.uk/browse/SASS-8996
  )

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined

  override val dependentPagesWhenAnswerChanges: List[Settable[_]] =
    List(Class4NonDivingExemptPage) // TODO add further pages in https://jira.tools.tax.service.gov.uk/browse/SASS-8996
}
