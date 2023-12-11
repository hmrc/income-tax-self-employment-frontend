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

package controllers.actions

import base.SpecBase.{businessId, convertScalaFuture, taxYear}
import cats.implicits._
import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.actions.SubmittedDataRetrievalActionImplSpec._
import models.common.JourneyAnswersContext
import models.database.UserAnswers
import models.journeys.Journey
import models.requests.OptionalDataRequest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import stubs.repositories.StubSessionRepository
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.auth.core._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class SubmittedDataRetrievalActionImplSpec extends AnyWordSpecLike with Matchers {

  "transform" should {
    "return the original request if it already contains answers for the journey" in new SubmittedTestData {
      def userAnswers         = createUserAnswers(Json.obj("anyOtherIncome" -> "bar")).some
      def submittedUerAnswers = Json.obj("anyOtherIncome" -> "foo")
      def journey             = Journey.Income

      val result = underTest.transform(request).futureValue
      result shouldBe request
    }

    "persist journey answers as user answers if no user answers exist" in new SubmittedTestData {
      def userAnswers         = None
      def submittedUerAnswers = Json.obj("anyOtherIncome" -> "foo")
      def journey             = Journey.Income

      val result = underTest.transform(request).futureValue

      assertOptionalDataRequest(
        result,
        request.copy(userAnswers = UserAnswers("userId", Json.obj("SJPR05893938418" -> Json.obj("anyOtherIncome" -> "foo"))).some)
      )
    }

    // TODO Maybe I need to move getSubmitted before get Data then I have load dat for free
    "persist journey answers as user answers if user answers exist but without the specific journey" in new SubmittedTestData {
      def userAnswers         = createUserAnswers(Json.obj("anyOtherExpenses" -> "bar")).some
      def submittedUerAnswers = Json.obj("anyOtherIncome" -> "foo")
      def journey             = Journey.Income

      val result = underTest.transform(request).futureValue
      assertOptionalDataRequest(
        result,
        request.copy(userAnswers = UserAnswers(
          "userId",
          Json
            .obj(
              "SJPR05893938418" -> Json.obj(
                "anyOtherExpenses" -> "bar",
                "anyOtherIncome"   -> "foo"
              ))).some)
      )
    }

  }

  private def assertOptionalDataRequest[A](actual: OptionalDataRequest[A], expected: OptionalDataRequest[A]) = {
    val now = Instant.now()
    actual.request shouldBe expected.request
    actual.userId shouldBe expected.userId
    actual.user shouldBe expected.user
    actual.userAnswers.map(_.copy(lastUpdated = now)) shouldBe expected.userAnswers.map(_.copy(lastUpdated = now))
  }
}

object SubmittedDataRetrievalActionImplSpec {
  trait SubmittedTestData {
    def userAnswers: Option[UserAnswers]
    def submittedUerAnswers: JsObject
    def journey: Journey

    lazy val service = SelfEmploymentServiceStub(
      submittedAnswers = submittedUerAnswers.some.asRight
    )
    val repo = StubSessionRepository()
    val ctx  = JourneyAnswersContext(taxYear, businessId, _, journey)
    val user = User(mtditid = "1234567890", arn = None, nino = "AA112233A", AffinityGroup.Individual.toString)

    val request = OptionalDataRequest[AnyContentAsEmpty.type](FakeRequest(), "userId", user, userAnswers)

    val underTest = new SubmittedDataRetrievalActionImpl[JsObject](ctx, service, repo)
  }

  private def createUserAnswers(jsonObject: JsObject) = UserAnswers("userId", Json.obj(businessId.value -> jsonObject))

}
