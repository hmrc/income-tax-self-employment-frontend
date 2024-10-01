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

import base.SpecBase.{businessId, taxYear}
import cats.data.EitherT
import models.common.UserType
import models.database.UserAnswers
import models.errors.ServiceError
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class HowMuchTradingAllowanceSummarySpec extends AnyWordSpec with Matchers {

  private val id       = "some_id"
  private val authUser = UserType.Individual

  private val turnoverIncomeAmountPageData     = Json.obj("turnoverIncomeAmount" -> 456.00)
  private val maxTradingAllowancePageData      = Json.obj("howMuchTradingAllowance" -> "maximum")
  private val lessThanTradingAllowancePageData = Json.obj("howMuchTradingAllowance" -> "lessThan")

  private val otherData = Json.obj(businessId.value -> Json.obj("otherPage" -> 123.45))

  private val completeUserAnswersWithMaxTradingAllowance =
    UserAnswers(id, Json.obj(businessId.value -> (turnoverIncomeAmountPageData ++ maxTradingAllowancePageData)))

  private val completeUserAnswersWithMinimumTradingAllowance =
    UserAnswers(id, Json.obj(businessId.value -> (turnoverIncomeAmountPageData ++ lessThanTradingAllowancePageData)))

  private val userAnswersForTradingAllowanceOnly = UserAnswers(id, Json.obj(businessId.value -> maxTradingAllowancePageData))

  private val otherUserAnswers = UserAnswers(id, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "HowMuchTradingAllowanceSummary" when {
    "user answers for HowMuchTradingAllowancePage exist" when {
      "the maximum trading allowance is selected" when {
        "user answers exist for TurnoverIncomeAmountPage" should {
          "generate a summary list row (bundled in a Right) where the monetary value is taken from the TurnoverIncomeAmountPage answer" in {
            val resultT = EitherT(HowMuchTradingAllowanceSummary.row(completeUserAnswersWithMaxTradingAllowance, taxYear, authUser, businessId))

            resultT.map { row =>
              row.key.content shouldBe Text("howMuchTradingAllowance.checkYourAnswersLabel.individual")
              row.value.content shouldBe HtmlContent("The maximum £456.00")
            }

          }
        }
        "user answers don't exist for TurnoverIncomeAmountPage" should {
          "return an error in a Left" in {
            val result = HowMuchTradingAllowanceSummary.row(userAnswersForTradingAllowanceOnly, taxYear, authUser, businessId)

            result should matchPattern { case Some(Left(_: ServiceError)) =>
            }
          }
        }
      }
      "less than the maximum trading allowance is selected" should {
        "generate a summary list row where the value is quoted as `A lower amount`" in {
          val resultT = EitherT(HowMuchTradingAllowanceSummary.row(completeUserAnswersWithMinimumTradingAllowance, taxYear, authUser, businessId))

          resultT.map { row =>
            row.key.content shouldBe Text("howMuchTradingAllowance.checkYourAnswersLabel.individual")
            row.value.content shouldBe HtmlContent("common.lowerAmount")
          }
        }
      }
    }
    "user answers do not exist for HowMuchTradingAllowancePage" should {
      "return None" in {
        val result = HowMuchTradingAllowanceSummary.row(otherUserAnswers, taxYear, authUser, businessId)

        result shouldBe None
      }
    }
  }

}
