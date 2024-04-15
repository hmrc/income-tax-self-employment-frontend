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

package pages.expenses.interest

import controllers.journeys.expenses.interest.routes
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.DisallowableInterestPage
import play.api.mvc.Call

case object InterestAmountPage extends OneQuestionPage[BigDecimal] {
  override def toString: String = "interestAmount"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    if (hasDisallowable(businessId, userAnswers)) routes.InterestDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
    else routes.InterestCYAController.onPageLoad(taxYear, businessId)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined &&
      (!hasDisallowable(businessId, userAnswers) || InterestDisallowableAmountPage.hasAllFurtherAnswers(businessId, userAnswers))

  private def hasDisallowable(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(DisallowableInterestPage, businessId).getOrElse(false)
}
