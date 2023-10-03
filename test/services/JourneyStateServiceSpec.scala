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
import models.errors.{HttpError, HttpErrorBody}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import service.JourneyStateService
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class JourneyStateServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttp = mock[HttpClient]
  val mockAppConfig = mock[FrontendAppConfig]
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
//  val connector = new SelfEmploymentConnector(mockHttp, mockAppConfig)
  val selfEmploymentService = new JourneyStateService(mockConnector)

  val nino = "AA112233A"
  val journey = "journeyId"
  val taxYear = LocalDate.now().getYear
  val businessId = journey + "-" + nino
  val mtditid = "mtditid"

  "saveJourneyState" - {

    "must return a Right(()) when the connector returns a successful SelfEmploymentResponse" in {

//      when(mockHttp.PUT[String, SelfEmploymentResponse]("", "")
      when(mockConnector.saveJourneyState(businessId, journey, taxYear, complete = true, mtditid)
      ) thenReturn Future(Right(Some(true)))

      val result = await(selfEmploymentService.saveJourneyState(businessId, journey, taxYear, complete = true, mtditid))
      result mustBe Right(Some(true))
    }

    "must return a Left(APIErrorModel) when the connector returns an error SelfEmploymentResponse" in {
      val invalidNinoResponse = HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("400", "Error"))
      when(mockConnector.saveJourneyState("fakeBusinessId", journey, taxYear, complete = true, mtditid)
      ) thenReturn Future(Left(invalidNinoResponse))

      val result = await(selfEmploymentService.saveJourneyState("fakeBusinessId", journey, taxYear, complete = true, mtditid))
      result mustBe Left(invalidNinoResponse)
    }

  }

}
