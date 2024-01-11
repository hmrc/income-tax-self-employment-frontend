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

package models.database

import models.common.BusinessId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}

class UserAnswersSpec extends AnyWordSpecLike with Matchers {
  private val businessId = BusinessId("businessId")

  "upsertFragment" should {
    "set a new value" in {
      val userAnswers = UserAnswers("userId")
      val result      = userAnswers.upsertFragment(businessId, Json.obj("key" -> "value"))
      result.data shouldBe Json.obj(
        businessId.value -> Json.obj("key" -> "value")
      )
    }

    "merge two jsons if they have mutually exclusive keys" in {
      val existingData: JsObject = Json.obj(
        "businessId" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )
      )

      val userAnswers = UserAnswers("userId", existingData)

      val result = userAnswers.upsertFragment(businessId, Json.obj("key3" -> "value3"))

      result.data shouldBe Json.obj(
        businessId.value -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2",
          "key3" -> "value3"
        )
      )
    }

    "merge two jsons if they DOES NOT have mutually exclusive keys" in {
      val existingData: JsObject = Json.obj(
        "businessId" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )
      )

      val userAnswers = UserAnswers("userId", existingData)

      val result = userAnswers.upsertFragment(businessId, Json.obj("key2" -> "valueUpdated"))

      result.data shouldBe Json.obj(
        businessId.value -> Json.obj(
          "key1" -> "value1",
          "key2" -> "valueUpdated"
        )
      )
    }

    "merge two jsons with the businessId if many businessIds present" in {
      val existingData: JsObject = Json.obj(
        "businessId_A" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        ),
        "businessId_B" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )
      )

      val userAnswers = UserAnswers("userId", existingData)

      val result = userAnswers.upsertFragment(BusinessId("businessId_A"), Json.obj("key2" -> "valueUpdated"))

      result.data shouldBe Json.obj(
        "businessId_A" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "valueUpdated"
        ),
        "businessId_B" -> Json.obj(
          "key1" -> "value1",
          "key2" -> "value2"
        )
      )
    }

  }

}
