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

package viewmodels.checkAnswers.expenses.advertisingOrMarketing

import controllers.journeys.expenses.advertisingOrMarketing.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing.YesDisallowable
import pages.expenses.advertisingOrMarketing._
import pages.expenses.tailoring.individualCategories.AdvertisingOrMarketingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object AdvertisingDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(AdvertisingOrMarketingPage, Some(businessId))
      .filter(areAnyAdvertisingDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(AdvertisingOrMarketingDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(AdvertisingOrMarketingAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"advertisingDisallowableAmount.title.$userType", allowableAmount),
      "advertisingDisallowableAmount.change.hidden"
    )

  private def areAnyAdvertisingDisallowable(advertisingAnswer: AdvertisingOrMarketing): Boolean =
    advertisingAnswer match {
      case YesDisallowable => true
      case _               => false
    }

}
