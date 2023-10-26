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

import models.UserAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class TradingAllowanceSummarySpec extends AnyWordSpec with Matchers {

  private val id         = "some_id"
  private val businessId = "some_business_id"
  private val taxYear    = 2024
  private val authUser   = "agent"

  private val data          = Json.obj(businessId -> Json.obj("tradingAllowance" -> "useTradingAllowance"))
  private val someOtherData = Json.obj(businessId -> Json.obj("someOtherPage" -> "some_other_value"))

  private val userAnswers          = UserAnswers(id, data)
  private val someOtherUserAnswers = UserAnswers(id, someOtherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "TradingAllowanceSummary" when {
    "user answers for TradingAllowancePage exist" should {
      "generate a summary list row" in {
        val result = TradingAllowanceSummary.row(userAnswers, taxYear, authUser, businessId)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text("tradingAllowance.checkYourAnswersLabel.agent")
        result.get.value.content shouldBe HtmlContent("tradingAllowance.useTradingAllowance")
      }
    }
  }

  "user answers do not exist for TradingAllowancePage" should {
    "return None" in {
      val result = TradingAllowanceSummary.row(someOtherUserAnswers, taxYear, authUser, businessId)

      result shouldBe None
    }
  }

}
