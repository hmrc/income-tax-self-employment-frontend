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

package viewmodels.checkAnswers.expenses.staffCosts

import base.SpecBase.{businessId, taxYear, userAnswersId}
import builders.UserBuilder.aNoddyUser
import common.TestApp.buildAppWithMessages
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.OptionValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor3}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class StaffCostsDisallowableAmountSummarySpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {
  implicit val messages: Messages = buildAppWithMessages()

  // @formatter:off
  val cases: TableFor3[JsObject, Int, Option[String]] = Table(
    ("json", "expected", "expectedDisallowableStaffCosts"),
    (Json.obj(
      "disallowableStaffCosts" -> false
    ), 0, None),
    (Json.obj(
      "disallowableStaffCosts" -> true,
    ), 0, None),
    (Json.obj(
      "disallowableStaffCosts" -> false,
      "staffCostsAmount" -> BigDecimal(123.45)
    ), 0, None),
    (Json.obj(
      "disallowableStaffCosts" -> true,
      "staffCostsAmount" -> BigDecimal(123.45)
    ), 0, None),
    (Json.obj(
      "disallowableStaffCosts" -> true,
      "staffCostsDisallowableAmount" -> BigDecimal(200.45)
    ), 0, None),
    (Json.obj(
      "disallowableStaffCosts" -> true,
      "staffCostsAmount" -> BigDecimal(100.456),
      "staffCostsDisallowableAmount" -> BigDecimal(200.0)
    ), 1, Some("100.46")),
    (Json.obj(
      "disallowableStaffCosts" -> true,
      "staffCostsAmount" -> BigDecimal(50.454),
      "staffCostsDisallowableAmount" -> BigDecimal(200.0)
    ), 1, Some("50.45"))
  )
  // @formatter:on

  "row" should {
    "return correct number of rows for different combination of data" in {
      forAll(cases) { case (json, expected, expectedDisallowableStaffCosts) =>
        val request    = createRequest(json)
        val actualList = StaffCostsDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
        actualList.size shouldBe expected
        actualList.map(_.key).foreach { definedKey =>
          definedKey.content.asHtml.toString() should include(expectedDisallowableStaffCosts.value)
        }
      }
    }
  }

  def createRequest(json: JsObject): DataRequest[AnyContentAsEmpty.type] = {
    val data        = Json.obj(businessId.value -> json)
    val userAnswers = UserAnswers(userAnswersId, data)
    DataRequest(FakeRequest(), userAnswersId, aNoddyUser, userAnswers)
  }

}
