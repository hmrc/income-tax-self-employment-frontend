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

package viewmodels.checkAnswers.expenses.repairsandmaintenance

import base.SpecBase.{taxYear, businessId, stubbedBusinessId, userAnswersId}
import builders.UserBuilder.aNoddyUser
import common.TestApp.buildAppWithMessages
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import org.scalatest.OptionValues._

class RepairsAndMaintenanceDisallowableAmountSummarySpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {
  implicit val messages: Messages = buildAppWithMessages()

  // @formatter:off
  val cases = Table(
    ("json", "expected", "expectedDisallowable"),
    (Json.obj(
      "repairsAndMaintenance" -> "no"
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesAllowable",
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesAllowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(123.45)
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(123.45)
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceDisallowableAmount" -> BigDecimal(200.45)
    ), 0, None),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(100.456),
      "repairsAndMaintenanceDisallowableAmount" -> BigDecimal(200.0)
    ), 1, Some("100.46")),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(50.454),
      "repairsAndMaintenanceDisallowableAmount" -> BigDecimal(200.0)
    ), 1, Some("50.45"))
  )
  // @formatter:on

  "row" should {
    "return correct number of rows for different combination of data" in {
      forAll(cases) { case (json, expected, expectedDisallowable) =>
        val request    = createRequest(json)
        val actualList = RepairsAndMaintenanceDisallowableAmountSummary.row(request, taxYear, businessId)
        actualList.size shouldBe expected
        actualList.map(_.key).foreach { definedKey =>
          definedKey.content.asHtml.toString() should include(expectedDisallowable.value)
        }
      }
    }
  }

  def createRequest(json: JsObject) = {
    val data        = Json.obj(stubbedBusinessId -> json)
    val userAnswers = UserAnswers(userAnswersId, data)
    DataRequest(FakeRequest(), userAnswersId, aNoddyUser, userAnswers)
  }

}
