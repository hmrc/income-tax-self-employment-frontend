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
import connectors.httpParser.JourneyStateParser.JourneyStateResponse
import org.scalamock.handlers.CallHandler6
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
trait MockSelfEmploymentConnector extends MockFactory {
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  def mockSaveJourneyState(nino: String, journeyId: String, taxYear: Int, complete: Boolean, mtditid: String, response: JourneyStateResponse):
    CallHandler6[String, String, Int, Boolean, String, HeaderCarrier, Future[JourneyStateResponse]] = {
      (mockConnector.saveJourneyState(_: String, _: String, _: Int, _: Boolean, _: String)(_: HeaderCarrier))
        .expects(nino, journeyId, taxYear, complete, mtditid, *)
        .returns(Future.successful(response))
        .anyNumberOfTimes()
    }
}
