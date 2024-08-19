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

package viewmodels.checkAnswers.expenses.advertisingOrMarketing

import base.summaries.SummaryBaseSpec
import models.common.UserType
import models.database.UserAnswers
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney

class AdvertisingDisallowableAmountSummarySpec extends SummaryBaseSpec("AdvertisingDisallowableAmountSummary") {

  private val allowableAmount: BigDecimal    = 500
  private val disallowableAmount: BigDecimal = 20

  override val validData: JsObject = Json.obj(
    "advertisingOrMarketing"                   -> "yesDisallowable",
    "advertisingOrMarketingAmount"             -> allowableAmount,
    "advertisingOrMarketingDisallowableAmount" -> disallowableAmount
  )
  override val invalidData: JsObject = Json.obj("otherPage" -> disallowableAmount)

  override val testKey: UserType => Text = (userType: UserType) => Text(messages(s"advertisingDisallowableAmount.title.$userType", allowableAmount))
  override val testValue: HtmlContent    = HtmlContent(s"£${formatMoney(disallowableAmount, addDecimalForWholeNumbers = false)}")

  override def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow] =
    AdvertisingDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType)(messages)

}
