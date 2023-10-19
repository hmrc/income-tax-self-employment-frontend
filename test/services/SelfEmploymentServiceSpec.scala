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
import builders.BusinessDataBuilder.aBusinessData
import builders.TradesJourneyStatusesBuilder.aSequenceTadesJourneyStatusesModel
import connectors.SelfEmploymentConnector
import models.errors.{HttpError, HttpErrorBody}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val service: SelfEmploymentService         = new SelfEmploymentService(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val nino       = "nino"
  val businessId = "businessId"
  val mtditid    = "mtditid"
  val accrual    = "ACCRUAL"

  "getCompletedTradeDetails" - {
    "should return a Right(Seq(TradesJourneyStatuses)) when this is returned from the backend" in {
      when(mockConnector.getCompletedTradesWithStatuses(meq(nino), meq(taxYear), meq(mtditid))(any, any)) thenReturn Future(
        Right(aSequenceTadesJourneyStatusesModel))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))(10.seconds)

      result mustEqual Right(aSequenceTadesJourneyStatusesModel)
    }
    "should return a Left(HttpError) when a this is returned from the backend" in {
      when(mockConnector.getCompletedTradesWithStatuses(meq(nino), meq(taxYear), meq(mtditid))(any, any)) thenReturn Future(
        Left(HttpError(404, HttpErrorBody.parsingError)))

      val result = await(service.getCompletedTradeDetails(nino, taxYear, mtditid))(10.seconds)

      result mustEqual Left(HttpError(404, HttpErrorBody.parsingError))
    }
  }

  "getBusinessAccountingType" - {
    "should return a BusinessID's accounting type in a Right when this is returned from the backend" in {
      when(mockConnector.getBusiness(meq(nino), meq(businessId), meq(mtditid))(any, any)) thenReturn Future(Right(aBusinessData))

      val result = await(service.getBusinessAccountingType(nino, businessId, mtditid))(10.seconds)

      result mustEqual Right(accrual)
    }

    "should return a Left(HttpError) when" - {

      "an empty sequence is returned from the backend" in {
        when(mockConnector.getBusiness(meq(nino), meq(businessId), meq(mtditid))(any, any)) thenReturn Future(Right(Seq.empty))

        val result = await(service.getBusinessAccountingType(nino, businessId, mtditid))(10.seconds)

        result mustEqual Left(HttpError(NOT_FOUND, HttpErrorBody.SingleErrorBody("404", "Business not found")))
      }

      "a Left(HttpError) is returned from the backend" in {
        when(mockConnector.getBusiness(meq(nino), meq(businessId), meq(mtditid))(any, any)) thenReturn Future(
          Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)))

        val result = await(service.getBusinessAccountingType(nino, businessId, mtditid))(10.seconds)

        result mustEqual Left(HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError))
      }
    }
  }

}
