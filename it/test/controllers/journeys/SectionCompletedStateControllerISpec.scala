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

package controllers.journeys

import base.IntegrationBaseSpec
import helpers.{AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers._

class SectionCompletedStateControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val journey: Journey = Journey.Income

  private val url: String =
    routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, journey, NormalMode).url

  private val submitUrl: String =
    routes.SectionCompletedStateController.onSubmit(taxYear, businessId, journey, NormalMode).url

  private val completedSectionUrl: String =
    s"/income-tax-self-employment/completed-section/${businessId.value}/$journey/${taxYear.endYear}"

  "GET /:taxYear/:businessId/:journey/details/completed-section" when {
    "the user is authorised and the journey status is not found" must {
      "return OK with a blank form" in {
        AuthStub.authorised()
        stubGetWithResponseBody(completedSectionUrl, NOT_FOUND, "")

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None
      }
    }

    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        stubGetWithResponseBody(completedSectionUrl, NOT_FOUND, "")

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

  "POST /:taxYear/:businessId/:journey/details/completed-section" when {
    "the user submits without selecting an option" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()

        val result = await(buildClient(submitUrl).post(Map[String, Seq[String]]("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }

    "the user selects 'Yes' (completed)" must {
      "save the status and redirect to the task list" in {
        AuthStub.authorised()
        stubPutWithResponseBody(completedSectionUrl, OK, "{}")

        val result = await(buildClient(submitUrl).post(Map[String, Seq[String]]("value" -> Seq("true"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }

    "the user selects 'No' (in progress)" must {
      "save the status and redirect to the task list" in {
        AuthStub.authorised()
        stubPutWithResponseBody(completedSectionUrl, OK, "{}")

        val result = await(buildClient(submitUrl).post(Map[String, Seq[String]]("value" -> Seq("false"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }
  }
}
