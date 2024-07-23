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

import base.SpecBase._
import builders.TradesJourneyStatusesBuilder._
import cats.data.EitherT
import cats.implicits._
import connectors.SelfEmploymentConnector
import models.common.{JourneyAnswersContext, Mtditid, Nino, TaxYear}
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import models.journeys.{Journey, TaskList, TaskListWithRequest}
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchersSugar._
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository

import scala.concurrent.Future

class SubmittedDataRetrievalActionProviderImplSpec extends AnyWordSpecLike with Matchers with ScalaFutures with MockitoSugar {

  "apply" should {
    "return a SubmittedDataRetrievalActionImpl" in new TestCase {
      val underTest = new SubmittedDataRetrievalActionProviderImpl(connector, repo)
      val ctx: OptionalDataRequest[_] => JourneyAnswersContext =
        req => JourneyAnswersContext(taxYear, req.nino, businessId, req.mtditid, Journey.TradeDetails)
      val result = underTest[JsObject](ctx)
      result shouldBe a[SubmittedDataRetrievalActionImpl[_]]
    }
  }

  "loadTaskList" should {
    "should return a task list for multiple journeys and businesses" in new TestCase {
      connector.getTaskList(*[Nino], *[TaxYear], *[Mtditid])(*, *) returns EitherT.rightT[Future, ServiceError](aTaskList)
      connector.getSubmittedAnswers[JsObject](*)(*, *, *) returns EitherT.rightT[Future, ServiceError](None)
      repo.set(*) returns Future.successful(true)

      val underTest = new SubmittedDataRetrievalActionProviderImpl(connector, repo)

      val result = await(underTest.loadTaskList(taxYear, fakeOptionalRequest).value)

      val expected = TaskListWithRequest(aTaskList, fakeOptionalRequest)
      result shouldBe Right(expected)
    }

    "should return an error if connector fails" in new TestCase {
      connector.getTaskList(*[Nino], *[TaxYear], *[Mtditid])(*, *) returns EitherT.leftT[Future, TaskList](
        ServiceError.ConnectorResponseError("method", "url", HttpError(404, HttpErrorBody.parsingError)))

      val underTest = new SubmittedDataRetrievalActionProviderImpl(connector, repo)

      val result = underTest.loadTaskList(taxYear, fakeOptionalRequest).value.futureValue

      result shouldBe Left(ServiceError.ConnectorResponseError("method", "url", HttpError(404, HttpErrorBody.parsingError)))
    }
  }

  trait TestCase {
    val connector = mock[SelfEmploymentConnector]
    val repo      = mock[SessionRepository]
  }
}
