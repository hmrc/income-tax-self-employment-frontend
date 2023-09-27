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
import connectors.httpParser.SelfEmploymentResponse.SelfEmploymentResponse
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class SelfEmploymentService @Inject()(connector: SelfEmploymentConnector) extends Logging {

  def saveJourneyState(businessId: String, journey: String, taxYear: Int, complete: Boolean): Future[SelfEmploymentResponse] = {
    connector.saveJourneyState(businessId, journey, taxYear, complete)(new HeaderCarrier())
  }

  def getJourneyState(businessId: String, journey: String, taxYear: Int): Future[SelfEmploymentResponse] = {
    connector.getJourneyState(businessId, journey, taxYear)(new HeaderCarrier())
  }
}
