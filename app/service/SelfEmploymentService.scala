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
import connectors.httpParsers.SelfEmploymentResponse.SelfEmploymentResponse
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentService @Inject()(connector: SelfEmploymentConnector)
                                     (implicit ec: ExecutionContext) extends Logging {

  def saveJourneyState(nino: String, journeyId: String, isComplete: Boolean): Future[SelfEmploymentResponse] = {
    connector.saveJourneyState(nino, journeyId, isComplete)(new HeaderCarrier(), ec)
  }
}
