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
import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class OfficeSuppliesAmountSummarySpec extends SpecBase {

  private val userType = individual

  private val data      = Json.obj(businessId.value -> Json.obj("officeSuppliesAmount" -> 123.45))
  private val otherData = Json.obj("otherPage" -> 123.45)

  private val userAnswers      = UserAnswers(userAnswersId, data)
  private val otherUserAnswers = UserAnswers(userAnswersId, otherData)

  private implicit val messages: Messages = messagesStubbed

  "OfficeSuppliesAmountSummary" - {
    "user answers for OfficeSuppliesAmountPage exist" - {
      "generate a summary list row" in {
        val result = OfficeSuppliesAmountSummary.row(userAnswers, taxYear, businessId, userType)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text(s"officeSuppliesAmount.title.$userType")
        result.get.value.content shouldBe Text("£123.45")
      }
    }
    "user answers do not exist for OfficeSuppliesAmountPage" - {
      "return None" in {
        val result = OfficeSuppliesAmountSummary.row(otherUserAnswers, taxYear, businessId, userType)

        result shouldBe None
      }
    }
  }

}
