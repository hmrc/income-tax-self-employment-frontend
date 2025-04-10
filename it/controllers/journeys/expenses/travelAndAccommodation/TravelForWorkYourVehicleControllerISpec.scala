package controllers.journeys.expenses.travelAndAccommodation

import base.IntegrationBaseSpec
import common.SessionValues
import forms.expenses.travelAndAccommodation.TravelForWorkYourVehicleFormProvider
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.ExpensesVehicleDetails
import models.common.{JourneyAnswersContext, UserType}
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation.{FlatRate, VehicleDetailsDb}
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourVehicleView

class TravelForWorkYourVehicleControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, NormalMode).url
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
      "return OK with the correct view" ignore {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)

        val fakeRequest = FakeRequest("GET", url)
          .withHeaders(
            HeaderNames.AUTHORIZATION -> "Bearer BXQ3/Treo4kQCZvVcCqKPhA7wE/2hNqCz4BjnFzEN5m6lmzrFrQI96Au2BZrW0e9WLpDsptzxUoUEaw0V1MQH6EXGq/8151X26j/qnvuZUXEsWcJ6ru7Fr+/ci2kcBf4NHKTPCIju1pIGJG5Oqihp7aDpRrleO+Ik/A5cDedlvf9KwIkeIPK/mMlBESjue4V")
          .withSession(
            SessionValues.CLIENT_MTDITID -> mtditid.value,
            SessionValues.CLIENT_NINO    -> nino.value
          )

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.body mustBe view(
          form(UserType.Agent),
          NormalMode,
          UserType.Agent,
          taxYear,
          businessId
        )(fakeRequest, messagesApi.preferred(fakeRequest)).toString
//        redirectLocation(result) mustBe None
//        status(result) mustBe OK
//        contentAsString(result) mustBe view(
//          form(UserType.Agent),
//          NormalMode,
//          UserType.Agent,
//          taxYear,
//          businessId
//        )(fakeRequest, messagesApi.preferred(fakeRequest)).toString
      }

      "return OK and pre-populate the field when the user has data" in {
//        AuthStub.agentAuthorised()
//        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testVehicleDetails)))

      }
    }
    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()

      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.authorised()
      }
    }
  }

}
