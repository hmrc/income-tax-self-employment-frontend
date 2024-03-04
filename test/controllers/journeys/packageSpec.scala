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

package controllers.journeys

import builders.UserBuilder.aNoddyUser
import models.{CheckMode, NormalMode}
import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpecLike
import pages.OneQuestionPage
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import queries.Settable

import scala.concurrent.ExecutionContext.Implicits.global

class packageSpec extends AnyWordSpecLike with ScalaFutures {
  lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
  val businessId                                           = BusinessId("businessId")

  "clearPages" when {
    "clearPagesWhenNo" should {
      object Page1 extends OneQuestionPage[Boolean] {
        override def toString: String = "page1"

        override val dependentPagesWhenNo: List[Settable[_]] = List(
          Page2,
          Page3
        )
      }
      object Page2 extends OneQuestionPage[Boolean] {
        override def toString: String = "page2"
      }
      object Page3 extends OneQuestionPage[Boolean] {
        override def toString: String = "page3"
      }

      val validData: JsObject = Json.obj(
        businessId.value -> Json.obj(
          "page1" -> true,
          "page2" -> 20.0,
          "page3" -> 30.0
        ))
      val answers     = UserAnswers("id", validData)
      val dataRequest = DataRequest(getRequest, "userId", aNoddyUser, answers)

      "do not change answer if selected Yes (true)" in {
        val (updatedAnswer, _) = clearPagesWhenNo(Page1, true, dataRequest, NormalMode, businessId).futureValue
        assert(updatedAnswer === answers)
      }

      "remove dependent pages if selected No (false)" in {
        val (updatedAnswer, _) = clearPagesWhenNo(Page1, false, dataRequest, NormalMode, businessId).futureValue
        val expectedData = Json.obj(
          businessId.value -> Json.obj(
            "page1" -> true
          ))
        assert(updatedAnswer.data === UserAnswers("id", expectedData).data)
      }

      "do not change mode if the previously persisted answer was Yes (true)" in {
        val (_, mode) = clearPagesWhenNo(Page1, false, dataRequest, NormalMode, businessId).futureValue
        assert(mode === NormalMode)
      }

      "stay in Normal mode if there was no previous answer" in {
        val (_, mode) =
          clearPagesWhenNo(Page1, false, dataRequest.copy(userAnswers = UserAnswers("id", JsObject.empty)), NormalMode, businessId).futureValue
        assert(mode === NormalMode)
      }

      "stay in Check mode if there was no previous answer" in {
        val (_, mode) =
          clearPagesWhenNo(Page1, false, dataRequest.copy(userAnswers = UserAnswers("id", JsObject.empty)), CheckMode, businessId).futureValue
        assert(mode === CheckMode)
      }

      "return NormalMode if changing answer from No -> Yes" in {
        val (_, mode) = clearPagesWhenNo(
          Page1,
          true,
          dataRequest.copy(
            userAnswers = UserAnswers(
              "id",
              Json.obj(
                businessId.value -> Json.obj(
                  "page1" -> false
                )))),
          CheckMode,
          businessId).futureValue
        assert(mode === NormalMode)
      }
    }

    "clearPagesWhenYes" should {
      object Page1 extends OneQuestionPage[Boolean] {
        override def toString: String = "page1"

        override val dependentPagesWhenYes: List[Settable[_]] = List(
          Page2,
          Page3
        )
      }
      object Page2 extends OneQuestionPage[Boolean] {
        override def toString: String = "page2"
      }
      object Page3 extends OneQuestionPage[Boolean] {
        override def toString: String = "page3"
      }

      val validData: JsObject = Json.obj(
        businessId.value -> Json.obj(
          "page1" -> true,
          "page2" -> 20.0,
          "page3" -> 30.0
        ))
      val answers     = UserAnswers("id", validData)
      val dataRequest = DataRequest(getRequest, "userId", aNoddyUser, answers)

      "do not change answer if selected No (false)" in {
        val (updatedAnswer, _) = clearPagesWhenYes(Page1, false, dataRequest, NormalMode, businessId).futureValue
        assert(updatedAnswer === answers)
      }

      "remove dependent pages if selected Yes (true)" in {
        val (updatedAnswer, _) = clearPagesWhenYes(Page1, true, dataRequest, NormalMode, businessId).futureValue
        val expectedData = Json.obj(
          businessId.value -> Json.obj(
            "page1" -> true
          ))
        assert(updatedAnswer.data === UserAnswers("id", expectedData).data)
      }

      "do not change mode if the previously persisted answer was No (false)" in {
        val (_, mode) = clearPagesWhenYes(
          Page1,
          true,
          dataRequest.copy(userAnswers = UserAnswers(
            "id",
            Json.obj(
              businessId.value -> Json.obj(
                "page1" -> false,
                "page2" -> 20.0,
                "page3" -> 30.0
              )))),
          NormalMode,
          businessId
        ).futureValue
        assert(mode === NormalMode)
      }

      "stay in Normal mode if there was no previous answer" in {
        val (_, mode) =
          clearPagesWhenYes(Page1, false, dataRequest.copy(userAnswers = UserAnswers("id", JsObject.empty)), NormalMode, businessId).futureValue
        assert(mode === NormalMode)
      }

      "stay in Check mode if there was no previous answer" in {
        val (_, mode) =
          clearPagesWhenYes(Page1, false, dataRequest.copy(userAnswers = UserAnswers("id", JsObject.empty)), CheckMode, businessId).futureValue
        assert(mode === CheckMode)
      }

      "return NormalMode if changing answer from Yes -> No" in {
        val (_, mode) = clearPagesWhenYes(
          Page1,
          false,
          dataRequest.copy(
            userAnswers = UserAnswers(
              "id",
              Json.obj(
                businessId.value -> Json.obj(
                  "page1" -> true
                )))),
          CheckMode,
          businessId).futureValue
        assert(mode === NormalMode)
      }
    }

  }
}
