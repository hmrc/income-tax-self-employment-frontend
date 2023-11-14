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
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class OfficeSuppliesDisallowableAmountSummarySpec extends SpecBase {

  private val authUserType = individual
  private val businessId   = "some_business_id"

  private val validData   = Json.obj(businessId -> Json.obj("officeSuppliesAmount" -> 200.00, "officeSuppliesDisallowableAmount" -> 100.00))
  private val invalidData = Json.obj(businessId -> Json.obj("officeSuppliesDisallowableAmount" -> 100.00))
  private val otherData   = Json.obj(businessId -> Json.obj("otherPage" -> 300.00))

  private val validUserAnswers   = UserAnswers(userAnswersId, validData)
  private val invalidUserAnswers = UserAnswers(userAnswersId, invalidData)
  private val otherUserAnswers   = UserAnswers(userAnswersId, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "OfficeSuppliesDisallowableAmountSummary" - {
    "user answers for OfficeSuppliesAmountPage exist" - {
      "user answers for OfficeSuppliesDisallowableAmountPage exist" - {
        "generate a summary list row" in {
          val result = OfficeSuppliesDisallowableAmountSummary.row(validUserAnswers, taxYear, businessId, authUserType)

          result.get shouldBe a[SummaryListRow]
          result.get.key.content shouldBe Text(s"officeSuppliesDisallowableAmount.checkYourAnswersLabel.$authUserType")
          result.get.value.content shouldBe Text("£100.00")
        }
      }
      "user answers do not exist for OfficeSuppliesDisallowableAmountPage" - {
        "return None" in {
          val result = OfficeSuppliesDisallowableAmountSummary.row(otherUserAnswers, taxYear, businessId, authUserType)

          result shouldBe None
        }
      }
    }
    "no user answers exist for OfficeSuppliesAmountPage" - {
      "return None" in {
        val result = OfficeSuppliesDisallowableAmountSummary.row(invalidUserAnswers, taxYear, businessId, authUserType)

        result shouldBe None
      }
    }
  }

}
