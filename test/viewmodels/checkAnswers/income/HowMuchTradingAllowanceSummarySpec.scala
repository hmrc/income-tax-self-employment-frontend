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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class HowMuchTradingAllowanceSummarySpec extends AnyWordSpec with Matchers {

  private val id       = "some_id"
  private val taxYear  = 2024
  private val authUser = "individual"

  private val turnoverIncomeAmountPageData     = Json.obj("turnoverIncomeAmount" -> 456.00)
  private val maxTradingAllowancePageData      = Json.obj("howMuchTradingAllowance" -> "maximum")
  private val lessThanTradingAllowancePageData = Json.obj("howMuchTradingAllowance" -> "lessThan")

  private val otherData = Json.obj("otherPage" -> 123.45)

  private val completeUserAnswersWithMaxTradingAllowance     = UserAnswers(id, turnoverIncomeAmountPageData ++ maxTradingAllowancePageData)
  private val completeUserAnswersWithMinimumTradingAllowance = UserAnswers(id, turnoverIncomeAmountPageData ++ lessThanTradingAllowancePageData)

  private val userAnswersForTradingAllowanceOnly = UserAnswers(id, maxTradingAllowancePageData)

  private val otherUserAnswers = UserAnswers(id, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "HowMuchTradingAllowanceSummary" when {
    "user answers for HowMuchTradingAllowancePage exist" when {
      "the maximum trading allowance is selected" when {
        "user answers exist for TurnoverIncomeAmountPage" should {
          "generate a summary list row where the monetary value is taken from the TurnoverIncomeAmountPage answer" in {
            val result = HowMuchTradingAllowanceSummary.row(completeUserAnswersWithMaxTradingAllowance, taxYear, authUser)

            result.get shouldBe a[SummaryListRow]
            result.get.key.content shouldBe Text("howMuchTradingAllowance.checkYourAnswersLabel.individual")
            result.get.value.content shouldBe Text("The maximum £456.0")
          }
        }
        "user answers don't exist for TurnoverIncomeAmountPage" should {
          "throw a run-time exception" in {
            lazy val result = HowMuchTradingAllowanceSummary.row(userAnswersForTradingAllowanceOnly, taxYear, authUser)

            val exception = intercept[RuntimeException](result)

            exception.getMessage shouldBe "Unable to retrieve user answers for TurnoverIncomeAmountPage"
          }
        }
      }
      "less than the maximum trading allowance is selected" should {
        "generate a summary list row where the value is quoted as `A lower amount`" in {
          val result = HowMuchTradingAllowanceSummary.row(completeUserAnswersWithMinimumTradingAllowance, taxYear, authUser)

          result.get shouldBe a[SummaryListRow]
          result.get.key.content shouldBe Text("howMuchTradingAllowance.checkYourAnswersLabel.individual")
          result.get.value.content shouldBe Text("howMuchTradingAllowance.lowerAmount")
        }
      }
    }
    "user answers do not exist for HowMuchTradingAllowancePage" should {
      "return None" in {
        val result = HowMuchTradingAllowanceSummary.row(otherUserAnswers, taxYear, authUser)

        result shouldBe None
      }
    }
  }

}
