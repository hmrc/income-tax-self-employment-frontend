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
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

abstract case class SummaryBaseSpec(summaryName: String) extends SpecBase {

  private val userTypes: List[UserType] = List(Individual, Agent)

  implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  val validData: JsObject
  val invalidData: JsObject = Json.obj("otherPage" -> 123.45)

  val testKey: UserType => Text
  val testValue: HtmlContent

  private lazy val validAnswers: UserAnswers   = buildUserAnswers(validData)
  private lazy val invalidAnswers: UserAnswers = buildUserAnswers(invalidData)

  /** method under test */
  def buildSummaryListRow(userAnswers: UserAnswers, userType: UserType): Option[SummaryListRow]

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
