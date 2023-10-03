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

import connectors.SelfEmploymentConnector
import connectors.httpParser.GetTradesStatusHttpParser.GetTradesStatusResponse
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentService @Inject()(connector: SelfEmploymentConnector)(implicit ec: ExecutionContext) extends Logging {

  def getCompletedTradeDetails(nino: String, taxYear: Int, mtditid: String): Future[GetTradesStatusResponse] = {

//        connector.getCompletedTradesWithStatuses(nino, taxYear, mtditid)
    connector.getCompletedTradesWithStatusMock(nino, taxYear, mtditid)
  }

}
