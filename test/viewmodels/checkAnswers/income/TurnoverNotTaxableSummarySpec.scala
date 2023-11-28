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
import base.SpecBase.{businessId, stubbedBusinessId, taxYear}
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class TurnoverNotTaxableSummarySpec extends AnyWordSpec with Matchers {

  private val id        = "some_id"
  private val authUser  = "agent"
  private val data      = Json.obj(stubbedBusinessId -> Json.obj("turnoverNotTaxable" -> true))
  private val otherData = Json.obj(stubbedBusinessId -> Json.obj("otherPage" -> true))

  private val userAnswers      = UserAnswers(id, data)
  private val otherUserAnswers = UserAnswers(id, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "TurnoverNotTaxableSummary" when {
    "user answers for TurnoverNotTaxablePage exist" should {
      "generate a summary list row" in {
        val result = TurnoverNotTaxableSummary.row(userAnswers, taxYear, authUser, businessId)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text("income.turnoverExemptFromTax.agent")
        result.get.value.content shouldBe Text("site.yes")
      }
    }
    "user answers do not exist for TurnoverNotTaxablePage" should {
      "return None" in {
        val result = TurnoverNotTaxableSummary.row(otherUserAnswers, taxYear, authUser, businessId)

        result shouldBe None
      }
    }
  }

}
