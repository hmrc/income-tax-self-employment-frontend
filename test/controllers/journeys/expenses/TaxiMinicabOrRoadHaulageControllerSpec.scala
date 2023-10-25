package controllers.journeys.expenses

import base.SpecBase
import controllers.journeys.expenses.routes.TaxiMinicabOrRoadHaulageController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.TaxiMinicabOrRoadHaulageFormProvider
import models.journeys.TaxiMinicabOrRoadHaulage
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.TaxiMinicabOrRoadHaulagePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.TaxiMinicabOrRoadHaulageView

import scala.concurrent.Future

class TaxiMinicabOrRoadHaulageControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val taxiMinicabOrRoadHaulageRoute = TaxiMinicabOrRoadHaulageController.onPageLoad(NormalMode).url

  val formProvider = new TaxiMinicabOrRoadHaulageFormProvider()
  val form         = formProvider()

  "TaxiMinicabOrRoadHaulage Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxiMinicabOrRoadHaulageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxiMinicabOrRoadHaulageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TaxiMinicabOrRoadHaulagePage, TaxiMinicabOrRoadHaulage.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxiMinicabOrRoadHaulageRoute)

        val view = application.injector.instanceOf[TaxiMinicabOrRoadHaulageView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TaxiMinicabOrRoadHaulage.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxiMinicabOrRoadHaulageRoute)
            .withFormUrlEncodedBody(("value", TaxiMinicabOrRoadHaulage.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, taxiMinicabOrRoadHaulageRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TaxiMinicabOrRoadHaulageView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, taxiMinicabOrRoadHaulageRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, taxiMinicabOrRoadHaulageRoute)
            .withFormUrlEncodedBody(("value", TaxiMinicabOrRoadHaulage.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
