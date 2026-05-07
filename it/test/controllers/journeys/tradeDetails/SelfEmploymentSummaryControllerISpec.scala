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
import helpers.{AuthStub, SelfEmploymentApiStub, WiremockSpec}
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.libs.json.Json

class SelfEmploymentSummaryControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val url: String = routes.SelfEmploymentSummaryController.onPageLoad(taxYear, businessId).url

  private val getBusinessesUrl: String =
    s"/income-tax-self-employment/individuals/business/details/${nino.value}/list"

  "GET /:taxYear/self-employment-summary" when {
    "the user is authorised" must {
      "return OK with an empty summary when no businesses are found" in {
        AuthStub.authorised()
        stubGetWithResponseBody(getBusinessesUrl, OK, "[]", headersSentToBE)

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None
      }

      "return OK with a summary when businesses are found" in {
        AuthStub.authorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(OK, Json.toJson(businessData))
        stubGetWithResponseBody(getBusinessesUrl, OK, Json.stringify(Json.arr(Json.toJson(businessData))), headersSentToBE)

        val result = await(buildClient(url).get())

        result.status mustBe OK
      }
    }

    "the user is an agent" must {
      "return OK with the summary view" in {
        AuthStub.agentAuthorised()
        stubGetWithResponseBody(getBusinessesUrl, OK, "[]")

        val result = await(buildClient(url, isAgent = true).get())

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
