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

package viewmodels.checkAnswers.expenses.workplaceRunningCosts

import base.summaries.SummaryBaseSpec
import models.common.UserType
import models.database.UserAnswers
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountPage
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney

class BusinessPremisesDisallowableAmountSummarySpec extends SummaryBaseSpec("BusinessPremisesDisallowableAmountSummary") {

  private val disallowableAmount: BigDecimal = 500
  private val allowableAmount: BigDecimal    = 700

  override val validData: JsObject = Json.obj(
    "businessPremisesDisallowableAmount" -> disallowableAmount,
    "businessPremisesAmount"             -> allowableAmount
  )
  override val invalidData: JsObject = Json.obj("otherPage" -> disallowableAmount)

  override val testKey: UserType => Text = (userType: UserType) => Text(s"businessPremisesDisallowableAmount.title.$userType")
  override val testValue: HtmlContent    = HtmlContent(s"£${formatMoney(disallowableAmount)}")

  override def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow] =
    BusinessPremisesDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType)(messages)

  "row" - {

    "return None if no allowable amount" in {
      val answers = buildUserAnswers(
        Json.obj(
          "businessPremisesDisallowableAmount" -> disallowableAmount
        ))
      val actual = BusinessPremisesDisallowableAmountSummary.row(answers, taxYear, businessId, UserType.Individual)
      assert(actual === None)
    }

    "return None if no disallowable amount" in {
      val answers = buildUserAnswers(
        Json.obj(
          "businessPremisesAmount" -> allowableAmount
        ))
      val actual = BusinessPremisesDisallowableAmountSummary.row(answers, taxYear, businessId, UserType.Individual)
      assert(actual === None)
    }

    "return non empty row if allowable and disallowable amount" in {
      val answers = buildUserAnswers(validData)
      val actual  = BusinessPremisesDisallowableAmountSummary.row(answers, taxYear, businessId, UserType.Individual)
      assert(actual.map(_.value.content) === Some(HtmlContent(s"£$disallowableAmount.00")))
    }

  }

}
