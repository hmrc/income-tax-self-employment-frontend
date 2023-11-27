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

import models.common.TaxYear
import models.common.UserType.Individual
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class SelfEmploymentAbroadSummarySpec extends AnyWordSpec with Matchers {

  private val id         = "some_id"
  private val taxYear    = TaxYear(2024)
  private val businessId = "some_business_id"

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

  private val userAnswers          = UserAnswers(id, data)
  private val someOtherUserAnswers = UserAnswers(id, someOtherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "SelfEmploymentAbroadSummary" when {
    "user answers for SelfEmploymentAbroadPage exist" should {
      "generate a summary list row" in {
        val result = SelfEmploymentAbroadSummary.row(taxYear, Individual, businessId, userAnswers)

        result shouldBe a[SummaryListRow]
        result.key.content shouldBe Text("selfEmploymentAbroad.title.individual")
        result.value.content shouldBe Text("site.yes")
      }
    }
    "no user answers exist for SelfEmploymentAbroadPage" should {
      "return None and throw runtime exception" in {
        lazy val result = SelfEmploymentAbroadSummary.row(taxYear, Individual, businessId, someOtherUserAnswers)

        val exception = intercept[RuntimeException](result)

        exception.getMessage shouldBe "No UserAnswers retrieved for SelfEmploymentAbroadPage"
      }
    }

  }

}
