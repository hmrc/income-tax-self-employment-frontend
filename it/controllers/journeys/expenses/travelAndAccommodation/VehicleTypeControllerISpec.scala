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
import forms.expenses.travelAndAccommodation.VehicleTypeFormProvider
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.ExpensesVehicleDetails
import models.common.JourneyAnswersContext
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation.{FlatRate, VehicleDetailsDb, VehicleType}
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.journeys.expenses.travelAndAccommodation.VehicleTypeView

class VehicleTypeControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  def onwardRoute: Call = Call("GET", "/foo")

  val url: String       = routes.VehicleTypeController.onPageLoad(taxYear, businessId, index, NormalMode).url
  val submitUrl: String = routes.VehicleTypeController.onSubmit(taxYear, businessId, index, NormalMode).url

  val formProvider                       = new VehicleTypeFormProvider()
  val form: Form[VehicleType]            = formProvider("vehicleName")
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesVehicleDetails)
  val vehicleName                        = "CarName"

  val testVehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("Car"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(FlatRate),
    costsOutsideFlatRate = Some(BigDecimal("100.00"))
  )

  val view        = app.injector.instanceOf[VehicleTypeView]
  val messagesApi = app.injector.instanceOf[MessagesApi]

  "GET /:taxYear/:businessId/expenses/travel/vehicle-type" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleType = None))))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        val ele = Jsoup.parse(result.body).select("input[id=value_0]")
        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        ele.`val`() mustBe "CarOrGoodsVehicle"
        ele.select("input[id=value_0]").hasAttr("checked") mustBe true
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleType = None))))
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

  "POST /:taxYear/:businessId/expenses/travel/vehicle-type" when {
    "the user selects a valid vehicle-type" must {
      "redirect to the next page" in {

        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(vehicleType = None))))
        AnswersApiStub.replaceIndex(testContext, Json.toJson(testVehicleDetails), index = 1)(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(VehicleType.values.head.toString))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, NormalMode).url)
      }
    }

    "the user submits without entering a vehicle description" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails)))
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }

}
