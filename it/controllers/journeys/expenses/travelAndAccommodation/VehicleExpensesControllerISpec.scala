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
import models.NormalMode
import models.common.Journey.ExpensesVehicleDetails
import models.common.JourneyAnswersContext
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.{LeasedVehicles, MyOwnVehicle}
import org.jsoup.Jsoup
import pages.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypePage
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers._

class VehicleExpensesControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.VehicleExpensesController.onPageLoad(taxYear, businessId, index, NormalMode).url
  val submitUrl: String                  = routes.VehicleExpensesController.onSubmit(taxYear, businessId, index, NormalMode).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesVehicleDetails)
  val userAnswers: UserAnswers = UserAnswers(internalId)
    .set(TravelAndAccommodationExpenseTypePage, Set[TravelAndAccommodationExpenseType](MyOwnVehicle, LeasedVehicles), Some(businessId))
    .toOption
    .value

  "GET /:taxYear/:businessId/expenses/travel/:index/vehicle-expenses " when {
    "the user is an agent" must {
      "return OK with the correct view" in {

        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleExpenses = None))))
        DbHelper.insertUserAnswers(userAnswers)

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())
        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value]").first().`val`() mustBe "300"
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleExpenses = None))))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
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

  "POST /:taxYear/:businessId/expenses/travel/:index/vehicle-expenses " when {
    "the user enters a valid vehicle expenses" must {
      "redirect to the next page" in {

        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleExpenses = None))))
        AnswersApiStub.replaceIndex(testContext, Json.toJson(testVehicleDetails), index = 1)(OK)
        DbHelper.insertUserAnswers(userAnswers)

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("100.00"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.TravelAndAccommodationExpensesCYAController.onPageLoad(taxYear, businessId).url)
      }
    }

    "the user submits without entering a vehicle expenses" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleExpenses = None))))
        DbHelper.insertUserAnswers(userAnswers)

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
