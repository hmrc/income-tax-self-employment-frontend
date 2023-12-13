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

package controllers.actions

import base.SpecBase.{businessId, taxYear}
import models.common.JourneyAnswersContext
import models.journeys.Journey
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.JsObject
import repositories.SessionRepository
import services.SelfEmploymentService

import scala.concurrent.ExecutionContext.Implicits.global

class SubmittedDataRetrievalActionProviderSpec extends AnyWordSpecLike with MockitoSugar with Matchers {

  "apply" should {
    "return a SubmittedDataRetrievalActionImpl" in {
      val service = mock[SelfEmploymentService]
      val repo    = mock[SessionRepository]

      val underTest = new SubmittedDataRetrievalActionProvider(service, repo)
      val result    = underTest[JsObject](JourneyAnswersContext(taxYear, businessId, _, Journey.TradeDetails))
      result shouldBe a[SubmittedDataRetrievalActionImpl[_]]
    }
  }
}
