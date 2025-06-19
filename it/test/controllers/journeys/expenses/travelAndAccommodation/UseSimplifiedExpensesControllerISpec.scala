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

package controllers.journeys.expenses.travelAndAccommodation

import base.IntegrationBaseSpec
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.common.Journey.ExpensesVehicleDetails
import models.common.JourneyAnswersContext
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers._

class UseSimplifiedExpensesControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId, index).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesVehicleDetails)

  "GET /:taxYear/:businessId/expenses/travel/:index/vehicle-flat-rate" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "redirect to 'there is a problem page' when VehicleDetailsDB data is 'None'" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.obj()))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe Some("/update-and-submit-income-tax-return/self-employment/there-is-a-problem")
        result.status mustBe SEE_OTHER
      }
    }

    "the user is unauthorized" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION).exists(_.contains("gg-sign-in")) mustBe true
        result.status mustBe SEE_OTHER
      }
    }
  }
}
