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

package config

import connectors.SelfEmploymentConnector
import connectors.httpParser.SelfEmploymentResponse.SelfEmploymentResponse
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
trait MockSelfEmploymentConnector extends MockFactory {
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  def mockSaveJourneyState(nino: String, journeyId: String, taxYear: Int, isComplete: Boolean, response: SelfEmploymentResponse):
    CallHandler5[String, String, Int, Boolean, HeaderCarrier, Future[SelfEmploymentResponse]] = {
      (mockConnector.saveJourneyState(_: String, _: String, _: Int, _: Boolean)(_: HeaderCarrier))
        .expects(nino, journeyId, taxYear, isComplete, *)
        .returns(Future.successful(response))
        .anyNumberOfTimes()
    }
}
