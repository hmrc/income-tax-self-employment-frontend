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

package viewmodels.checkAnswers.expenses.repairsandmaintenance

import base.SpecBase
import base.SpecBase.{businessId, taxYear, userAnswersId}
import builders.UserBuilder.aNoddyUser
import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest

class RepairsAndMaintenanceAmountSummarySpec extends AnyWordSpecLike with Matchers {
  val data                        = Json.obj(businessId.value -> Json.obj("repairsAndMaintenanceAmount" -> 123.45))
  val userAnswers                 = UserAnswers(userAnswersId, data)
  val request                     = DataRequest(FakeRequest(), userAnswersId, aNoddyUser, userAnswers)
  implicit val messages: Messages = SpecBase.messagesStubbed

  "row" should {
    "return defined rows for non empty value" in {
      RepairsAndMaintenanceAmountSummary.row(request, taxYear, businessId) shouldBe defined
    }

    "return no rows for no data" in {
      RepairsAndMaintenanceAmountSummary.row(request, taxYear, BusinessId("not-existing")) shouldBe empty
    }
  }

}
