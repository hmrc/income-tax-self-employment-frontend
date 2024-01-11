/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses.staffCosts

import base.SpecBase
import builders.UserBuilder.{aNoddyAgentUser, aNoddyUser}
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.requests.DataRequest
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class StaffCostsAmountSummarySpec extends SpecBase {

  private val data      = Json.obj(businessId.value -> Json.obj("staffCostsAmount" -> 2552.4))
  private val otherData = Json.obj(businessId.value -> Json.obj("otherPage" -> 123.45))

  private val userAnswers      = UserAnswers(userAnswersId, data)
  private val otherUserAnswers = UserAnswers(userAnswersId, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  case class UserScenario(userType: UserType, request: DataRequest[AnyContent])

  private val userScenarios = List(
    UserScenario(Individual, DataRequest(FakeRequest(), userAnswersId, aNoddyUser, userAnswers)),
    UserScenario(Agent, DataRequest(FakeRequest(), userAnswersId, aNoddyAgentUser, userAnswers))
  )

  "StaffCostsAmountSummary" - {
    "when user answers for StaffCostsAmountPage exist" - {
      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType} should" - {
          "generate a summary list row" in {
            val result = StaffCostsAmountSummary.row(userScenario.request, taxYear, businessId)

            result.get mustBe a[SummaryListRow]
            result.get.key.content mustBe Text(s"staffCostsAmount.title.${userScenario.userType}")
            result.get.value.content mustBe HtmlContent("£2,552.40")
          }
        }
      }
    }
    "when user answers do not exist for StaffCostsAmountPage should" - {
      "return None" in {
        val result = StaffCostsAmountSummary.row(DataRequest(FakeRequest(), userAnswersId, aNoddyUser, otherUserAnswers), taxYear, businessId)

        result mustBe None
      }
    }
  }

}
