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
import models.errors.APIErrorBody.APIStatusError
import models.errors.ServiceError
import models.requests.BusinessDataWithStatus
import models.viewModels.TaggedTradeDetailsViewModel
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentService @Inject()(connector: SelfEmploymentConnector)(implicit ec: ExecutionContext) extends Logging {

//  def getCompletedTradeDetails(nino: String, taxYear: Int): Future[Either[APIStatusError, Seq[TaggedTradeDetailsViewModel]]] = {
//    val result = connector.getTradesWithStatus(nino, taxYear) map {
//      case Left(error) => Left(error)
//      case Right(businessesData: Seq[BusinessDataWithStatus]) =>
//        val completedBusinessesList: Seq[BusinessDataWithStatus] = businessesData.filter(_.isCompleted)
//        Right(completedBusinessesList.map(bdws =>
//          (for {
//            abroadStatus <- FutureEitherOps[APIStatusError, String](checkSelfEmploymentAbroadStatus(taxYear, bdws.businessData.businessId).map(_ => "completed"))
//            incomeStatus <- FutureEitherOps[APIStatusError, String](checkSelfEmploymentAbroadStatus(taxYear, bdws.businessData.businessId).map(_ => "notStarted"))
//            expensesStatus <- FutureEitherOps[APIStatusError, String](checkSelfEmploymentAbroadStatus(taxYear, bdws.businessData.businessId).map(_ => "notStarted"))
//            nationalInsuranceStatus <- FutureEitherOps[APIStatusError, String](checkSelfEmploymentAbroadStatus(taxYear, bdws.businessData.businessId).map(_ => "notStarted"))
//            result = TaggedTradeDetailsViewModel(
//                bdws.businessData.tradingName,
//                abroadStatus,
//                incomeStatus,
//                expensesStatus,
//                nationalInsuranceStatus)
//          } yield {
//            result
//          }).value
//        ))
//    }
//    result
//  }

  def getCompletedTradeDetailsMock(nino: String, taxYear: Int): Future[Either[APIStatusError, Seq[TaggedTradeDetailsViewModel]]] = {
    Future(Right(Seq(
      TaggedTradeDetailsViewModel(Some("TradingName1"), "completed", "inProgress", "notStarted", "notStarted"),
      TaggedTradeDetailsViewModel(None, "notStarted", "notStarted", "notStarted", "notStarted")
    )))
  }

  private def checkSelfEmploymentAbroadStatus(taxYear: Int, businessId: String): Future[Either[APIStatusError, String]] = {
    //    connector.getJourneyState(taxYear, businessId, "self-employment-abroad") map {
    connector.getJourneyStateMock(taxYear, businessId, "self-employment-abroad") map {
      case Left(error) => Left(error)
      case Right(status) => Right(status)
    }
  }
}
