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

package viewmodels.checkAnswers.expenses.advertisingAndMarketing

import controllers.journeys.expenses
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing
import models.journeys.expenses.individualCategories.AdvertisingOrMarketing.YesDisallowable
import pages.expenses.advertisingAndMarketing.{AdvertisingAndMarketingAmountPage, AdvertisingAndMarketingDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.AdvertisingOrMarketingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import utils.MoneyUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AdvertisingDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(AdvertisingOrMarketingPage, Some(businessId))
      .filter(areAnyAdvertisingDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(AdvertisingAndMarketingDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(AdvertisingAndMarketingAmountPage, Some(businessId))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"advertisingDisallowableAmount.title.$userType", allowableAmount),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = s"£${formatMoney(disallowableAmount)}",
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          expenses.advertisingAndMarketing.routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages("advertisingDisallowableAmount.change.hidden"))
      )
    )

  private def areAnyAdvertisingDisallowable(advertisingAnswer: AdvertisingOrMarketing): Boolean =
    advertisingAnswer match {
      case YesDisallowable => true
      case _               => false
    }

}
