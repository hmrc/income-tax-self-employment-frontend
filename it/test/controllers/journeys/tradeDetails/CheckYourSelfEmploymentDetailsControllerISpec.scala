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

package controllers.journeys.tradeDetails

import base.IntegrationBaseSpec
import controllers.standard.routes._
import helpers.{AuthStub, SelfEmploymentApiStub, WiremockSpec}
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.libs.json.Json

class CheckYourSelfEmploymentDetailsControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val url: String = routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url

  "GET /:taxYear/:businessId/check-your-self-employment-details" when {

    "the user is authorised" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(OK, Json.toJson(businessData))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None
      }
    }

    "the user is an agent" must {
      "return OK with the correct view for agent" in {
        AuthStub.agentAuthorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(OK, Json.toJson(businessData))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.status mustBe OK
      }
    }

    "the API returns a not found error" must {
      "redirect to journey recovery" in {
        AuthStub.authorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value mustBe JourneyRecoveryController.onPageLoad().url
      }
    }

    "the user is unauthorised" must {
      "redirect to login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("/gg-sign-in")
      }
    }
  }
}
