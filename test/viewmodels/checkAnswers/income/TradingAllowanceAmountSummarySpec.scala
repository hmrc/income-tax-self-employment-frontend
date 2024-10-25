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

package viewmodels.checkAnswers.income
import base.SpecBase.{buildUserAnswers, businessId, taxYear}
import models.common.UserType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class TradingAllowanceAmountSummarySpec extends AnyWordSpec with Matchers {

  private def buildRow(data: JsObject): Option[SummaryListRow] =
    TradingAllowanceAmountSummary.row(buildUserAnswers(data), taxYear, UserType.Individual, businessId)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "TradingAllowanceAmountSummary" should {
    "generate a summary list row" when {
      "user answers for TradingAllowanceAmountPage exist and HowMuchTradingAllowancePage is 'LessThan'" in {
        val data   = Json.obj("tradingAllowanceAmount" -> 123.45, "howMuchTradingAllowance" -> "lessThan")
        val result = buildRow(data)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text("tradingAllowanceAmount.title.individual")
        result.get.value.content shouldBe HtmlContent("£123.45")
      }
    }
    "return None" when {
      "user answers do not exist for TradingAllowanceAmountPage" in {
        val data   = Json.obj("howMuchTradingAllowance" -> "lessThan")
        val result = buildRow(data)

        result shouldBe None
      }
      "user answers do not exist for HowMuchTradingAllowancePage" in {
        val data   = Json.obj("tradingAllowanceAmount" -> 123.45)
        val result = buildRow(data)

        result shouldBe None
      }
      "user answers for HowMuchTradingAllowancePage are 'Maximum'" in {
        val data   = Json.obj("tradingAllowanceAmount" -> 123.45, "howMuchTradingAllowance" -> "maximum")
        val result = buildRow(data)

        result shouldBe None
      }
    }
  }

}
