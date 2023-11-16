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

package viewmodels.checkAnswers.expenses.goodsToSellOrUse

import base.SpecBase
import models.database.UserAnswers
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class DisallowableGoodsToSellOrUseAmountSummarySpec extends SpecBase {

  "DisallowableGoodsToSellOrUseAmountSummary" - {
    "some GoodsToSellOrUse were claimed to be disallowable" - {
      "user answers for GoodsToSellOrUseAmountPage exist" - {
        "user answers for DisallowableGoodsToSellOrUseAmountPage exist" - {
          "generate a summary list row" in new Test {
            val result = DisallowableGoodsToSellOrUseAmountSummary.row(validUserAnswers, taxYear, stubbedBusinessId, userType)

            result.get mustBe a[SummaryListRow]
            result.get.key.content mustBe Text(s"disallowableGoodsToSellOrUseAmount.title.$userType")
            result.get.value.content mustBe Text("£100.00")
          }
        }

        "user answers do not exist for DisallowableGoodsToSellOrUseAmountPage" - {
          "return None" in new Test {
            val result = DisallowableGoodsToSellOrUseAmountSummary.row(otherUserAnswers, taxYear, stubbedBusinessId, userType)

            result mustBe None
          }
        }
      }
      "no user answers exist for GoodsToSellOrUseAmountPage" - {
        "return None" in new Test {
          val result = DisallowableGoodsToSellOrUseAmountSummary.row(invalidUserAnswers, taxYear, stubbedBusinessId, userType)

          result mustBe None
        }
      }
    }
    "when no GoodsToSellOrUse are disallowable" - {
      "return None" in new Test {
        val result = DisallowableGoodsToSellOrUseAmountSummary.row(invalidUserAnswersAllAllowable, taxYear, stubbedBusinessId, individual)

        result mustBe None
      }
    }
  }

  trait Test {
    protected val userType: String = individual

    protected val validData: JsObject = Json
      .parse(s"""
           |{
           |  "$stubbedBusinessId": {
           |    "goodsToSellOrUse": "yesDisallowable",
           |    "goodsToSellOrUseAmount": 200.00,
           |    "disallowableGoodsToSellOrUseAmount": 100.00
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]

    protected val invalidDataAllAllowable: JsObject = Json
      .parse(s"""
           |{
           |  "$stubbedBusinessId": {
           |    "goodsToSellOrUse": "yesAllowable"
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]

    protected val invalidData: JsObject = Json
      .parse(s"""
           |{
           |  "$stubbedBusinessId": {
           |    "goodsToSellOrUse": "yesDisallowable",
           |    "disallowableGoodsToSellOrUseAmount": 100.00
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]

    protected val otherData: JsObject = Json
      .parse(s"""
           |{
           |  "$stubbedBusinessId": {
           |    "otherPage": "otherAnswer"
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]

    protected val validUserAnswers: UserAnswers               = UserAnswers(userAnswersId, validData)
    protected val invalidUserAnswers: UserAnswers             = UserAnswers(userAnswersId, invalidData)
    protected val invalidUserAnswersAllAllowable: UserAnswers = UserAnswers(userAnswersId, invalidDataAllAllowable)
    protected val otherUserAnswers: UserAnswers               = UserAnswers(userAnswersId, otherData)

    protected implicit val messages: MessagesImpl = {
      val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
      MessagesImpl(Lang("en"), messagesApi)
    }

  }

}
