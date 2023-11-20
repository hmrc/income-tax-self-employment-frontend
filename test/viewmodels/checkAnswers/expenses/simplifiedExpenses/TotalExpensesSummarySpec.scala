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

package viewmodels.checkAnswers.expenses.simplifiedExpenses

import base.SpecBase
import models.database.UserAnswers
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class TotalExpensesSummarySpec extends SpecBase {

  private val data      = Json.obj(stubbedBusinessId -> Json.obj("totalExpenses" -> 2552.4))
  private val otherData = Json.obj(stubbedBusinessId -> Json.obj("otherPage" -> 123.45))

  private val userAnswers      = UserAnswers(userAnswersId, data)
  private val otherUserAnswers = UserAnswers(userAnswersId, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  private val userTypes = List(individual, agent)

  "TotalExpensesSummarySpec" - {
    "when user answers for TotalExpensesPage exist" - {
      userTypes.foreach { userType =>
        s"when user is an $userType should" - {
          "generate a summary list row" in {
            val result = TotalExpensesSummary.row(userAnswers, taxYear, stubbedBusinessId, userType)

            result.get mustBe a[SummaryListRow]
            result.get.key.content mustBe Text(s"totalExpenses.title.$userType")
            result.get.value.content mustBe Text("£2,552.40")
          }
        }
      }
    }
    "when user answers do not exist for TotalExpensesPage should" - {
      "return None" in {
        val result = TotalExpensesSummary.row(otherUserAnswers, taxYear, stubbedBusinessId, individual)

        result mustBe None
      }
    }
  }

}
