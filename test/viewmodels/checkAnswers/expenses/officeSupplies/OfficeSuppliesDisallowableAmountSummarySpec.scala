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

package viewmodels.checkAnswers.expenses.officeSupplies

import base.SpecBase
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class OfficeSuppliesDisallowableAmountSummarySpec extends SpecBase {

  "OfficeSuppliesDisallowableAmountSummary" - {
    "some office supplies were claimed to be disallowable" - {
      "user answers for OfficeSuppliesAmountPage exist" - {
        "user answers for OfficeSuppliesDisallowableAmountPage exist" - {
          "generate a summary list row" in new Test {
            val result: Option[SummaryListRow] =
              OfficeSuppliesDisallowableAmountSummary.row(validUserAnswers, taxYear, businessId, userType)

            result.get shouldBe a[SummaryListRow]
            result.get.key.content shouldBe Text(s"officeSuppliesDisallowableAmount.title.$userType")
            result.get.value.content shouldBe Text("£100.00")
          }
        }
        "user answers do not exist for OfficeSuppliesDisallowableAmountPage" - {
          "return None" in new Test {
            val result: Option[SummaryListRow] =
              OfficeSuppliesDisallowableAmountSummary.row(otherUserAnswers, taxYear, businessId, userType)

            result shouldBe None
          }
        }
      }
      "no user answers exist for OfficeSuppliesAmountPage" - {
        "return None" in new Test {
          val result: Option[SummaryListRow] =
            OfficeSuppliesDisallowableAmountSummary.row(invalidUserAnswers, taxYear, businessId, userType)

          result shouldBe None
        }
      }
    }
    "no office supplies are disallowable" - {
      "return None" in new Test {
        val result: Option[SummaryListRow] =
          OfficeSuppliesDisallowableAmountSummary.row(invalidUserAnswersAllAllowable, taxYear, businessId, userType)

        result shouldBe None
      }
    }
  }

  trait Test {
    protected val userType: String = individual

    protected val validData: JsObject = Json
      .parse(s"""
      |{
      |  "$businessId": {
      |    "officeSupplies": "yesDisallowable",
      |    "officeSuppliesAmount": 200.00,
      |    "officeSuppliesDisallowableAmount": 100.00
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

    protected val invalidDataAllAllowable: JsObject = Json
      .parse(s"""
      |{
      |  "$businessId": {
      |    "officeSupplies": "yesAllowable"
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

    protected val invalidData: JsObject = Json
      .parse(s"""
      |{
      |  "$businessId": {
      |    "officeSupplies": "yesDisallowable",
      |    "officeSuppliesDisallowableAmount": 100.00
      |  }
      |}
      |""".stripMargin)
      .as[JsObject]

    protected val otherData: JsObject = Json
      .parse(s"""
      |{
      |  "$businessId": {
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
