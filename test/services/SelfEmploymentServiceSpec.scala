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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.SelfEmploymentConnector
import connectors.httpParsers.SelfEmploymentResponse.SelfEmploymentResponse
import models.errors.APIErrorBody
import models.errors.APIErrorBody.APIStatusError
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import service.SelfEmploymentService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttp = mock[HttpClient]
  val mockAppConfig = mock[FrontendAppConfig]
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
//  val connector = new SelfEmploymentConnector(mockHttp, mockAppConfig)
  val selfEmploymentService = new SelfEmploymentService(mockConnector)

  val nino = "AA112233A"
  val journey = "journeyId"
  val taxYear = LocalDate.now().getYear

  "saveJourneyState" - {

    "must return a Right(()) when the connector returns a successful SelfEmploymentResponse" in {

//      when(mockHttp.PUT[String, SelfEmploymentResponse]("", "")
      when(mockConnector.saveJourneyState(nino, journey, taxYear, complete = true)
      ) thenReturn Future(Right(()))

      val result = await(selfEmploymentService.saveJourneyState(nino, journey, taxYear, isComplete = true))
      result mustBe Right(())
    }

    "must return a Left(APIErrorModel) when the connector returns an error SelfEmploymentResponse" in {
      val invalidNinoResponse = APIStatusError(BAD_REQUEST, APIErrorBody.APIError("400", "Error"))
      when(mockConnector.saveJourneyState("fakeNino", journey, taxYear, complete = true)
      ) thenReturn Future(Left(invalidNinoResponse))

      val result = await(selfEmploymentService.saveJourneyState("fakeNino", journey, taxYear, isComplete = true))
      result mustBe Left(invalidNinoResponse)
    }

  }

}
