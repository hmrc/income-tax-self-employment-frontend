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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney

class AdvertisingAmountSummarySpec extends SummaryBaseSpec("AdvertisingAmountSummary") {

  private lazy val allowableAmount: BigDecimal = 500

  override lazy val validData: JsObject = Json.obj(
    "advertisingOrMarketing"       -> "yesDisallowable",
    "advertisingOrMarketingAmount" -> allowableAmount
  )
  override lazy val invalidData: JsObject = Json.obj("otherPage" -> allowableAmount)

  override lazy val testKey: UserType => Text = (userType: UserType) => Text(s"advertisingAmount.title.$userType")
  override lazy val testValue: Text           = Text(s"£${formatMoney(allowableAmount)}")

  override def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow] =
    AdvertisingAmountSummary.row(userAnswers, taxYear, businessId, userType)(messages)

}
