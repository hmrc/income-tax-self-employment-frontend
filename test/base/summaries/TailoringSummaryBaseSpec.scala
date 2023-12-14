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

package base.summaries

import base.SpecBase
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.JsObject
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

abstract case class TailoringSummaryBaseSpec(summaryName: String) extends SpecBase {

  private val userTypes: List[UserType] = List(Individual, Agent)

  protected implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  protected val validData: JsObject
  protected val invalidData: JsObject

  protected val testKey: UserType => Text
  protected val testValue: Text

  private val validAnswers: UserAnswers   = buildUserAnswers(validData)
  private val invalidAnswers: UserAnswers = buildUserAnswers(invalidData)

  protected val buildSummaryListRow: (UserAnswers, UserType) => Option[SummaryListRow]

  s"$summaryName should" - {
    userTypes.foreach { userType =>
      s"when user answers exist and user is an $userType" - {
        "generate a summary list row" in {
          val result = buildSummaryListRow(validAnswers, userType)

          result.get mustBe a[SummaryListRow]
          result.get.key.content mustBe testKey(userType)
          result.get.value.content mustBe testValue
        }
      }
    }
    "when user answers for the page do not exist, should return None" in {
      val result = buildSummaryListRow(invalidAnswers, Individual)

      result mustBe None
    }
  }
}
