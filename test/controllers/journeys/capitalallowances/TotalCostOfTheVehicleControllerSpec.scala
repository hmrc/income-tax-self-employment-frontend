package controllers.journeys.capitalallowances

import base.SpecBase
import forms.capitalallowances.zeroEmissionGoodsVehicle.TotalCostOfTheVehicleFormProvider
import models.database.UserAnswers
import models.NormalMode
import navigation.{CapitalallowancesNavigator, FakeCapitalallowancesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.capitalallowances.zeroEmissionGoodsVehicle.TotalCostOfTheVehiclePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.capitalallowances.TotalCostOfTheVehicleView

import scala.concurrent.Future

class TotalCostOfTheVehicleControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TotalCostOfTheVehicleFormProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0

  lazy val totalCostOfTheVehicleRoute = routes.TotalCostOfTheVehicleController.onPageLoad(NormalMode).url

  "TotalCostOfTheVehicle Controller" - {

    "must return OK and the correct view for a GET" in {
      fail("TODO: Add a proper test here")
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      fail("TODO: Add a proper test here")
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[CapitalallowancesNavigator].toInstance(new FakeCapitalallowancesNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, totalCostOfTheVehicleRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      fail("TODO: Add a proper test here")
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, totalCostOfTheVehicleRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, totalCostOfTheVehicleRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
