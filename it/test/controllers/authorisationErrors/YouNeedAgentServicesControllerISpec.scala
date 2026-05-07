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

package controllers.authorisationErrors

import base.IntegrationBaseSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class YouNeedAgentServicesControllerISpec extends IntegrationBaseSpec {

  private val url: String = routes.YouNeedAgentServicesController.onPageLoad().url

  "GET /error/you-need-agent-services-account" when {
    "a GET request is made" must {
      "return UNAUTHORIZED" in {
        val request = FakeRequest(GET, url)
        val result  = route(app, request).value

        status(result) mustEqual UNAUTHORIZED
      }
    }
  }
}
