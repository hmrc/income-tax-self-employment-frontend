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

import base.SpecBase
import base.SpecBase.{currTaxYear, stubBusinessId, stubbedBusinessId, userAnswersId}
import builders.UserBuilder.aNoddyUser
import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.libs.json.Json

class RepairsAndMaintenanceDisallowableAmountSummarySpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {
  implicit val messages: Messages = SpecBase.messagesEn

  // @formatter:off
  val cases = Table(
    ("json", "expected"),
    (Json.obj(
      "repairsAndMaintenance" -> "no"
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesAllowable",
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesAllowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(123.45)
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(123.45)
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceDisallowableAmount" -> BigDecimal(200.45)
    ), 0),
    (Json.obj(
      "repairsAndMaintenance" -> "yesDisallowable",
      "repairsAndMaintenanceAmount" -> BigDecimal(100.45),
      "repairsAndMaintenanceDisallowableAmount" -> BigDecimal(200.45)
    ), 1)
  )
  // @formatter:on

  "row" should {
    "return correct number of rows for different combination of data" in {
      forAll(cases) { case (json, expected) =>
        val request = createRequest(json)
        RepairsAndMaintenanceDisallowableAmountSummary.row(request, currTaxYear, stubBusinessId).size shouldBe expected
      }
    }
  }

  def createRequest(json: JsObject) = {
    val data        = Json.obj(stubbedBusinessId -> json)
    val userAnswers = UserAnswers(userAnswersId, data)
    DataRequest(FakeRequest(), userAnswersId, aNoddyUser, userAnswers)
  }

}
