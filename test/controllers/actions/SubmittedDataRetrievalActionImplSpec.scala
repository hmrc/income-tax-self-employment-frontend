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
import cats.data.EitherT
import cats.implicits._
import connectors.SelfEmploymentConnector
import controllers.actions.AuthenticatedIdentifierAction.User
import models.common.{Journey, JourneyAnswersContext}
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchersSugar._
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import stubs.repositories.StubSessionRepository
import uk.gov.hmrc.auth.core._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmittedDataRetrievalActionImplSpec extends AnyWordSpecLike with Matchers with MockitoSugar {

  "transform" should {
    "return the original request if it already contains answers for the journey" in new SubmittedTestData {
      def userAnswers         = createUserAnswers(Json.obj("anyOtherIncome" -> "bar")).some
      def submittedUerAnswers = Json.obj("anyOtherIncome" -> "foo")
      def journey             = Journey.Income

      val result = underTest.transform(request).futureValue
      result shouldBe request
    }

    "persist journey answers as user answers if no user answers exist" when {
      "NationalInsuranceContributions journey, removing the Class 2 or Class 4 answers from the NICsAnswers wrapper" in new SubmittedTestData {
        def userAnswers = None

        def submittedUerAnswers = Json.obj("class2Answers" -> Json.obj("class2NICs" -> true), "class4Answers" -> Json.obj("class4NICs" -> false))

        def journey = Journey.NationalInsuranceContributions

        val result = underTest.transform(request).futureValue

        assertOptionalDataRequest(
          result,
          request.copy(userAnswers = UserAnswers("userId", Json.obj("SJPR05893938418" -> Json.obj("class2NICs" -> true, "class4NICs" -> false))).some)
        )
      }
      "a non-NationalInsuranceContributions journey" in new SubmittedTestData {
        def userAnswers = None

        def submittedUerAnswers = Json.obj("anyOtherIncome" -> "foo")

        def journey = Journey.Income

        val result = underTest.transform(request).futureValue

        assertOptionalDataRequest(
          result,
          request.copy(userAnswers = UserAnswers("userId", Json.obj("SJPR05893938418" -> Json.obj("anyOtherIncome" -> "foo"))).some)
        )
      }
    }

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

  trait SubmittedTestData {
    def userAnswers: Option[UserAnswers]
    def submittedUerAnswers: JsObject
    def journey: Journey

    val connector                                            = mock[SelfEmploymentConnector]
    val submittedAnswersResult: ApiResultT[Option[JsObject]] = EitherT.pure[Future, ServiceError](submittedUerAnswers.some)

    connector.getSubmittedAnswers[JsObject](*)(*, *, *) returns submittedAnswersResult

    val repo                                                 = StubSessionRepository()
    val ctx: OptionalDataRequest[_] => JourneyAnswersContext = req => JourneyAnswersContext(taxYear, req.nino, businessId, req.mtditid, journey)
    val user = User(mtditid = "1234567890", arn = None, nino = "AA112233A", AffinityGroup.Individual.toString)

    val request = OptionalDataRequest[AnyContentAsEmpty.type](FakeRequest(), "userId", user, userAnswers)

    val underTest = new SubmittedDataRetrievalActionImpl[JsObject](ctx, connector, repo)
  }

  private def createUserAnswers(jsonObject: JsObject) = UserAnswers("userId", Json.obj(businessId.value -> jsonObject))

}
