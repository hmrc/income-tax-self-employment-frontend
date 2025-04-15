package controllers.journeys.expenses.travelAndAccommodation

import base.IntegrationBaseSpec
import forms.expenses.travelAndAccommodation.TravelForWorkYourVehicleFormProvider
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.ExpensesVehicleDetails
import models.common.JourneyAnswersContext
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation.{FlatRate, VehicleDetailsDb}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.filters.csrf.{CSRF, CSRFFilter}
import play.api.test.CSRFTokenHelper._
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourVehicleView

class TravelForWorkYourVehicleControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String                  = routes.TravelForWorkYourVehicleController.onSubmit(taxYear, businessId, NormalMode).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesVehicleDetails)

  val testVehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("Car"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(FlatRate),
    costsOutsideFlatRate = Some(BigDecimal("100.00"))
  )

  val view        = app.injector.instanceOf[TravelForWorkYourVehicleView]
  val form        = new TravelForWorkYourVehicleFormProvider()
  val messagesApi = app.injector.instanceOf[MessagesApi]

  "GET /:taxYear/:businessId/expenses/travel/your-vehicle" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getIndex(testContext, index = 1)(NOT_FOUND)
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

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value]").first().`val`() mustBe "Car"
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(NOT_FOUND)
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

  "POST /:taxYear/:businessId/expenses/travel/your-vehicle" when {
    "the user enters a valid vehicle description" must {
      "redirect to the next page" in {

        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(OK, Some(Json.toJson(testVehicleDetails.copy(description = None))))
        AnswersApiStub.replaceIndex(testContext, Json.toJson(testVehicleDetails), index = 1)(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("Car"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode).url)
      }
    }

    "the user submits without entering a vehicle description" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getIndex(testContext, index = 1)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }

}
