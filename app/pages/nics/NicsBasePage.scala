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

import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.nics.ExemptionCategory
import pages.OneQuestionPage
import play.api.mvc.Call

trait NicsBasePage[A] extends OneQuestionPage[A] {
  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    ??? // TODO to be added in https://jira.tools.tax.service.gov.uk/browse/SASS-8727

  def redirectForExemptionCategory(userAnswers: UserAnswers, category: ExemptionCategory, onTrue: Call, onFalse: Call): Call =
    userAnswers.get(Class4ExemptionCategoryPage).map(seq => if (seq.contains(category)) onTrue else onFalse).get
}
