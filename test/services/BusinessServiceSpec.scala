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

package services

import base.{ControllerTestScenarioSpec, SpecBase}
import builders.BusinessDataBuilder.aBusinessData
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import controllers.actions.SubmittedDataRetrievalActionProvider
import models.common._
import models.domain.BusinessData
import models.errors.ServiceError
import models.errors.ServiceError.ConnectorResponseError
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import stubs.repositories.StubSessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessServiceSpec extends SpecBase with ControllerTestScenarioSpec {
  val nino              = Nino("nino")
  val businessIdAccrual = BusinessId("businessIdAccrual")
  val businessIdCash    = BusinessId("businessIdCash")

  "getBusinesses" - {
    "should return a list of user businesses from downstream" in new ServiceWithStubs {
      val businessesList = Seq(aBusinessData)
      mockConnector.getBusinesses(any[Nino], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](businessesList)

      val result = service.getBusinesses(nino, mtditid).value.futureValue

      result shouldBe businessesList.asRight
    }
    "should return a ServiceError from downstream" in new ServiceWithStubs {
      val businessesList = Seq(aBusinessData)
      mockConnector.getBusinesses(any[Nino], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Seq[BusinessData]](ConnectorResponseError)

      val result = service.getBusinesses(nino, mtditid).value.futureValue

      result shouldBe businessesList.asLeft
    }
  }

}

trait ServiceWithStubs {
  val mockConnector: SelfEmploymentConnector   = mock[SelfEmploymentConnector]
  val repository                               = StubSessionRepository()
  val mockSubmittedDataRetrievalActionProvider = mock[SubmittedDataRetrievalActionProvider]

  val service: BusinessService = new BusinessServiceImpl(mockConnector)
}
