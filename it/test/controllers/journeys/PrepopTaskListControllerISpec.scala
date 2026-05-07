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
import models.journeys.TaskList
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers._

class PrepopTaskListControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val url: String = routes.PrepopTaskListController.onPageLoad(taxYear).url

  private val taskListUrl: String =
    s"/income-tax-self-employment/${taxYear.endYear}/${nino.value}/task-list"

  "GET /:taxYear/task-list-prepop" when {
    "the user is authorised" must {
      "return OK with the prepop task list view" in {
        AuthStub.authorised()
        stubGetWithResponseBody(taskListUrl, OK, Json.stringify(Json.toJson(TaskList.empty)))

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None
      }
    }

    "the user is an agent" must {
      "return OK with the prepop task list view" in {
        AuthStub.agentAuthorised()
        stubGetWithResponseBody(taskListUrl, OK, Json.stringify(Json.toJson(TaskList.empty)))

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
