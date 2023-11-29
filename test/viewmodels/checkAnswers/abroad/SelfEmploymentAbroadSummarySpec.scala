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

package viewmodels.checkAnswers.abroad

import base.SpecBase
import models.common.UserType.Individual
import models.database.UserAnswers
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class SelfEmploymentAbroadSummarySpec extends SpecBase {

  private val data = Json
    .parse(s"""
       |{
       |"$businessId": {
       |  "selfEmploymentAbroad": true
       |  }
       |}
       |""".stripMargin)
    .as[JsObject]

  private val someOtherData = Json
    .parse(s"""
       |{
       |"$businessId": {
       |  "someOtherPage": true
       |  }
       |}
       |""".stripMargin)
    .as[JsObject]

  private val userAnswers          = UserAnswers(userAnswersId, data)
  private val someOtherUserAnswers = UserAnswers(userAnswersId, someOtherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "SelfEmploymentAbroadSummary" - {
    "when user answers for SelfEmploymentAbroadPage exist" - {
      "should generate a summary list row" in {
        val result = SelfEmploymentAbroadSummary.row(taxYear, Individual, businessId, userAnswers)

        result mustBe a[SummaryListRow]
        result.key.content mustBe Text("selfEmploymentAbroad.title.individual")
        result.value.content mustBe Text("site.yes")
      }
    }
    "when no user answers exist for SelfEmploymentAbroadPage" - {
      "should return None and throw runtime exception" in {
        lazy val result = SelfEmploymentAbroadSummary.row(taxYear, Individual, businessId, someOtherUserAnswers)

        val exception = intercept[RuntimeException](result)

        exception.getMessage mustBe "No UserAnswers retrieved for SelfEmploymentAbroadPage"
      }
    }

  }

}
