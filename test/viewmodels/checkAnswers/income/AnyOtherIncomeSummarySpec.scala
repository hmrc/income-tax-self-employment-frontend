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
import base.SpecBase
import models.database.UserAnswers
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class AnyOtherIncomeSummarySpec extends SpecBase {

  private val authUser = "individual"

  private val data          = Json.obj(businessId.value -> Json.obj("anyOtherIncome" -> true))
  private val someOtherData = Json.obj("someOtherPage" -> true)

  private val userAnswers          = UserAnswers(userAnswersId, data)
  private val someOtherUserAnswers = UserAnswers(userAnswersId, someOtherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "AnyOtherIncomeSummary" - {
    "when user answers for AnyOtherIncomePage exist" - {
      "should generate a summary list row" in {
        val result = AnyOtherIncomeSummary.row(userAnswers, taxYear, authUser, businessId)

        result.get mustBe a[SummaryListRow]
        result.get.key.content mustBe Text("anyOtherIncome.title.individual")
        result.get.value.content mustBe Text("site.yes")
      }
    }

    "when user answers do not exist for AnyOtherIncomePage" - {
      "should return None" in {
        val result = AnyOtherIncomeSummary.row(someOtherUserAnswers, taxYear, authUser, businessId)

        result mustBe None
      }
    }

  }

}
