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

package controllers.standard

import base.IntegrationBaseSpec
import helpers.{AuthStub, WiremockSpec}
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers._

class JourneyRecoveryControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val url: String = routes.JourneyRecoveryController.onPageLoad().url

  "GET /there-is-a-problem" when {
    "the user is authorised" must {
      "return OK with the start-again view when no continueUrl provided" in {
        AuthStub.authorised()

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()

        val result = await(buildClient(url).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("gg-sign-in")
      }
    }
  }
}
