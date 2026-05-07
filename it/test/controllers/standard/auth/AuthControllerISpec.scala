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

package controllers.standard.auth

import base.IntegrationBaseSpec
import helpers.{AuthStub, WiremockSpec}
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers._

class AuthControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val signOutUrl: String        = routes.AuthController.signOut().url
  private val signOutNoSurveyUrl: String = routes.AuthController.signOutNoSurvey.url

  "GET /account/sign-out-survey" when {
    "the user is authorised" must {
      "redirect to the sign-out URL with continue pointing to the exit survey" in {
        AuthStub.authorised()
        DbHelper.insertEmpty()

        val result = await(buildClient(signOutUrl).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()

        val result = await(buildClient(signOutUrl).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("gg-sign-in")
      }
    }
  }

  "GET /account/sign-out" when {
    "the user is authorised" must {
      "redirect to the sign-out URL with continue pointing to the signed-out page" in {
        AuthStub.authorised()
        DbHelper.insertEmpty()

        val result = await(buildClient(signOutNoSurveyUrl).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()

        val result = await(buildClient(signOutNoSurveyUrl).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("gg-sign-in")
      }
    }
  }
}
