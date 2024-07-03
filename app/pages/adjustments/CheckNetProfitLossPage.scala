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

package pages.adjustments

import controllers.journeys.adjustments.profitOrLoss.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import play.api.mvc.Call

// TODO 8907 should pages 1 + 2 in CheckMode go to CYA or always through this summary page?
//  If to CYA then this page object can be deleted.
//  If through this page then change CheckNetProfitLossController methods and Page 1 + 2 page object methods
case object CheckNetProfitLossPage extends AdjustmentsBasePage[Unit] {
  override def toString: String = "checkNetProfitLoss"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    routes.CheckNetProfitLossController.onPageLoad(taxYear, businessId)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean = true
}
