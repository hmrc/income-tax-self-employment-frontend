package controllers.journeys.expenses.travelAndAccommodation

import base.SpecBase
import controllers.journeys.expenses.routes
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class TravelAndAccommodationDisallowableExpensesControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new TravelAndAccommodationDisallowableExpensesFormProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0

  lazy val travelAndAccommodationDisallowableExpensesRoute = routes.TravelAndAccommodationDisallowableExpensesController.onPageLoad(NormalMode).url

  "TravelAndAccommodationDisallowableExpenses Controller" - {

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
            bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, travelAndAccommodationDisallowableExpensesRoute)
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
        val request = FakeRequest(GET, travelAndAccommodationDisallowableExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, travelAndAccommodationDisallowableExpensesRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
