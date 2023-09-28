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

package service

import connectors.SelfEmploymentConnector
import models.errors.{HttpError, ServiceError}
import models.requests.{BusinessDataWithStatus, TaggedTradeDetails}
import models.viewModels.TaggedTradeDetailsViewModel
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentService @Inject()(connector: SelfEmploymentConnector) extends Logging {

  def getCompletedTradeDetailsMock(nino: String, taxYear: Int, mtditid: String): Future[Either[HttpError, Seq[TaggedTradeDetails]]] = {
    connector.getTradesWithStatusMock(nino, taxYear, mtditid)
  }

//  private def checkSelfEmploymentAbroadStatus(taxYear: Int, businessId: String): Future[Either[HttpError, String]] = {
//    //    connector.getJourneyState(taxYear, businessId, "self-employment-abroad") map {
//    connector.getJourneyStateMock(taxYear, businessId, "self-employment-abroad") map {
//      case Left(error) => Left(error)
//      case Right(status) => Right(status)
//    }
//  }
}
