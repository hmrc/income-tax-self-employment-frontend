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

package viewmodels.checkAnswers.capitalallowances.specialTaxSites

import base.summaries.SummaryBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.common.UserType
import models.database.UserAnswers
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.time.LocalDate

class ContractStartDateSummarySpec extends SummaryBaseSpec("ContractStartDateSummary") {

  override def validData: JsObject = Json.obj("contractStartDate" -> LocalDate.of(2020, 2, 20))

  override val testKey: UserType => Text = (_: UserType) => Text(messages("contractStartDate.title.cya"))

  override val testValue: HtmlContent = HtmlContent("20 February 2020")

  override def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow] =
    ContractStartDateSummary.row(LocalDate.of(2020, 2, 20), taxYear, businessId, 0)(messages).some

}
