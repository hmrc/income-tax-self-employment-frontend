/*
 * Copyright 2025 HM Revenue & Customs
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

package pages.expenses.advertisingOrMarketing

import controllers.journeys.expenses.advertisingOrMarketing.routes
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.AdvertisingOrMarketingPage
import play.api.mvc.Call

case object AdvertisingOrMarketingAmountPage extends OneQuestionPage[BigDecimal] {
  override def toString: String = "advertisingOrMarketingAmount"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    if (hasDisallowable(businessId, userAnswers)) routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
    else cyaPage(taxYear, businessId)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined &&
      (!hasDisallowable(businessId, userAnswers) || AdvertisingOrMarketingDisallowableAmountPage.hasAllFurtherAnswers(businessId, userAnswers))

  private def hasDisallowable(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(AdvertisingOrMarketingPage, businessId).contains(AdvertisingOrMarketing.YesDisallowable)

  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

}
