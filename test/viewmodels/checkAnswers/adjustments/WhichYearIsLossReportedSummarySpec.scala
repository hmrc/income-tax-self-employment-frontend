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

package viewmodels.checkAnswers.adjustments

import models.common.UserType
import base.SpecBase.{businessId, taxYear}
import models.database.UserAnswers
import models.journeys.adjustments.WhichYearIsLossReported
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class WhichYearIsLossReportedSummarySpec extends AnyWordSpec with Matchers {

  private val id       = "some_id"
  private val userType = UserType.Individual

  private val data: JsObject =
    Json.obj(
      businessId.value -> Json.obj(
        "whichYearIsLossReported" -> WhichYearIsLossReported.Year2018to2019.toString,
        "unusedLossAmount"        -> BigDecimal(200)
      ))
  private val otherData = Json.obj(businessId.value -> Json.obj("otherPage" -> 123.45))

  private val userAnswers      = UserAnswers(id, data)
  private val otherUserAnswers = UserAnswers(id, otherData)

  private implicit val messages: MessagesImpl = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  "WhichYearIsLossReportedSummary" when {
    "user answers for WhichYearIsLossReportedPage exist" should {
      "generate a summary list row" in {
        val result = WhichYearIsLossReportedSummary.row(userAnswers, userType, taxYear, businessId)

        result.get shouldBe a[SummaryListRow]
        result.get.key.content shouldBe Text("whichYearIsLossReported.checkYourAnswersLabel.individual")
        result.get.value.content shouldBe HtmlContent("whichYearIsLossReported.2018to2019")
      }
    }
    "user answers do not exist for WhichYearIsLossReportedPage" should {
      "return None" in {
        val result = WhichYearIsLossReportedSummary.row(otherUserAnswers, userType, taxYear, businessId)

        result shouldBe None
      }
    }
  }
}
