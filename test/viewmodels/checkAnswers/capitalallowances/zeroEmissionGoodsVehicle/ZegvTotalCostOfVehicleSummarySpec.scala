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

package viewmodels.checkAnswers.capitalallowances.zeroEmissionGoodsVehicle

import base.summaries.SummaryBaseSpec
import models.common.UserType
import models.database.UserAnswers
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class ZegvTotalCostOfVehicleSummarySpec extends SummaryBaseSpec("ZegvTotalCostOfVehicleSummary") {

  override val validData: JsObject = Json.obj("zegvTotalCostOfVehicle" -> 20.00)

  override val testKey: UserType => Text = (_: UserType) => Text(messages("zegvTotalCostOfVehicle.title"))

  override val testValue: HtmlContent = HtmlContent("£20.00")

  override def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow] =
    ZegvTotalCostOfVehicleSummary.row(userAnswers, taxYear, businessId, userType)(messages)

}
