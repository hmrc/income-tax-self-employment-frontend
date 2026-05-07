/*
 * Copyright 2025 HM Revenue & Customs
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

package services.mocks

import models.session.SessionData
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.SessionDataService

import scala.concurrent.Future

trait MockSessionDataService extends MockitoSugar {

  val mockSessionDataService: SessionDataService = mock[SessionDataService]

  object MockSessionDataService {

    def getSessionData(sessionId: String)(resp: SessionData): Unit =
      when(mockSessionDataService.getSessionData(eqTo(sessionId))(any(), any()))
        .thenReturn(Future.successful(resp))

    def getSessionDataException(sessionId: String)(err: Throwable): Unit =
      when(mockSessionDataService.getSessionData(eqTo(sessionId))(any(), any()))
        .thenReturn(Future.failed(err))
  }

}
