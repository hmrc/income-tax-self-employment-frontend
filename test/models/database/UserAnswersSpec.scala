package models.database

import models.common.BusinessId
import org.scalatest.TryValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}

class UserAnswersSpec extends AnyWordSpecLike with Matchers {
  private val businessId = BusinessId("businessId")

  "upsertFragment" should {
    "set a new value" in {
      val userAnswers = UserAnswers("userId")
      val result      = userAnswers.upsertFragment(businessId, Json.obj("key" -> "value")).success.value
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

      val result = userAnswers.upsertFragment(businessId, Json.obj("key3" -> "value3")).success.value

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

      val result = userAnswers.upsertFragment(businessId, Json.obj("key2" -> "valueUpdated")).success.value

      result.data shouldBe Json.obj(
        businessId.value -> Json.obj(
          "key1" -> "value1",
          "key2" -> "valueUpdated"
        )
      )
    }

    "merge two jsons if they have more than one business ids" in {
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

      val result = userAnswers.upsertFragment(BusinessId("businessId_A"), Json.obj("key2" -> "valueUpdated")).success.value

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
