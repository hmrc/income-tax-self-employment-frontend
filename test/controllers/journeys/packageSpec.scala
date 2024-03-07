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
    "clearDependentPages on No" should {
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
          "page1" -> true
        ))
      val answers     = UserAnswers("id", validData)
      val dataRequest = DataRequest(getRequest, "userId", aNoddyUser, answers)

      "remove dependent pages if selected Yes (true)" in {
        val updatedAnswer = clearDependentPages(Page1, true, dataRequest, businessId).futureValue
        assert(updatedAnswer === answers)
      }

      "remove dependent pages if selected No (false)" in {
        val updatedAnswer = clearDependentPages(Page1, false, dataRequest, businessId).futureValue
        val expectedData = Json.obj(
          businessId.value -> Json.obj(
            "page1" -> true
          ))
        assert(updatedAnswer.data === UserAnswers("id", expectedData).data)
      }
    }

    "clearDependentPages on Yes" should {
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
          "page1" -> true
        ))
      val answers     = UserAnswers("id", validData)
      val dataRequest = DataRequest(getRequest, "userId", aNoddyUser, answers)

      "remove dependent pages if selected No (false)" in {
        val updatedAnswer = clearDependentPages(Page1, false, dataRequest, businessId).futureValue
        assert(updatedAnswer === answers)
      }

      "remove dependent pages if selected Yes (true)" in {
        val updatedAnswer = clearDependentPages(Page1, true, dataRequest, businessId).futureValue
        val expectedData = Json.obj(
          businessId.value -> Json.obj(
            "page1" -> true
          ))
        assert(updatedAnswer.data === UserAnswers("id", expectedData).data)
      }
    }

  }
}
