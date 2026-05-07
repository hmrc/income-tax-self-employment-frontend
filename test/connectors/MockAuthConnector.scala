/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate

import scala.concurrent.Future

trait MockAuthConnector extends MockitoSugar {

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  object MockAuthConnector {

    @SuppressWarnings(Array("unchecked"))
    private var stubbings: Map[Predicate, OngoingStubbing[Future[Any]]] = Map.empty

    def authorise[T](predicate: Predicate)(response: Future[T]): OngoingStubbing[Future[T]] =
      stubbings.get(predicate) match {
        case None =>
          val stubbing = when(mockAuthConnector.authorise[T](meq(predicate), any())(any(), any()))
            .thenReturn(response)
          stubbings = stubbings + (predicate -> stubbing.asInstanceOf[OngoingStubbing[Future[Any]]])
          stubbing
        case Some(existing) =>
          val stubbing = existing
            .asInstanceOf[OngoingStubbing[Future[T]]]
            .thenReturn(response)
          stubbings = stubbings + (predicate -> stubbing.asInstanceOf[OngoingStubbing[Future[Any]]])
          stubbing
      }

    def resetStubbing(): Unit = {
      stubbings = Map.empty
      Mockito.reset(mockAuthConnector)
    }
  }

}
