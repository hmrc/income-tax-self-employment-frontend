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
import models.common.UserType
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class OtherIncomeAmountSummarySpec extends AnyWordSpec with Matchers {

  private val id       = "some_id"
  private val authUser = UserType.Individual

  private val data      = Json.obj(businessId.value -> Json.obj("otherIncomeAmount" -> 123.45))
  private val otherData = Json.obj(businessId.value -> Json.obj("otherPage" -> 123.45))

  private val userAnswers      = UserAnswers(id, data)
  private val otherUserAnswers = UserAnswers(id, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "OtherIncomeAmountSummary" when {
    "user answers for OtherIncomeAmountPage exist" should {
      "generate a summary list row" in {
        val result = OtherIncomeAmountSummary.row(userAnswers, taxYear, authUser, businessId)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text("otherIncomeAmount.title.individual")
        result.get.value.content shouldBe HtmlContent("£123.45")
      }
    }
    "user answers do not exist for IncomeNotCountedAsTurnoverPage" should {
      "return None" in {
        val result = OtherIncomeAmountSummary.row(otherUserAnswers, taxYear, authUser, businessId)

        result shouldBe None
      }
    }
  }

}
