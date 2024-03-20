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

package viewmodels.checkAnswers.capitalallowances.writingDownAllowance

import controllers.journeys.capitalallowances.writingDownAllowance.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.writingDownAllowance.WdaSingleAssetClaimAmountsPage
import viewmodels.checkAnswers.BigDecimalSummary

final case class WdaSingleAssetClaimAmountsSummary(taxYear: TaxYear, businessId: BusinessId)
    extends BigDecimalSummary(
      WdaSingleAssetClaimAmountsPage,
      routes.WdaSingleAssetClaimAmountsController.onPageLoad(taxYear, businessId, CheckMode)
    )
