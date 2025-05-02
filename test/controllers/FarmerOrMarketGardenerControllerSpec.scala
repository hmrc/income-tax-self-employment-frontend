//package controllers
//
//import base.SpecBase
//import forms.FarmerOrMarketGardenerFormProvider
//import models.NormalMode
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.when
//import org.scalatestplus.mockito.MockitoSugar
//import pages.FarmerOrMarketGardenerPage
//import play.api.inject.bind
//import play.api.mvc.Call
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//import repositories.SessionRepository
//import views.html.FarmerOrMarketGardenerView
//
//import scala.concurrent.Future
//
//class FarmerOrMarketGardenerControllerSpec extends SpecBase with MockitoSugar {
//
//  def onwardRoute = Call("GET", "/foo")
//
//  val formProvider = new FarmerOrMarketGardenerFormProvider()
//  val form         = formProvider()
//
//  lazy val farmerOrMarketGardenerRoute = routes.FarmerOrMarketGardenerController.onPageLoad(NormalMode).url
//
//  "FarmerOrMarketGardener Controller" - {
//
//    "must return OK and the correct view for a GET" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, farmerOrMarketGardenerRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[FarmerOrMarketGardenerView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = UserAnswers(userAnswersId).set(FarmerOrMarketGardenerPage, true).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, farmerOrMarketGardenerRoute)
//
//        val view = application.injector.instanceOf[FarmerOrMarketGardenerView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockSessionRepository = mock[SessionRepository]
//
//      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[SessionRepository].toInstance(mockSessionRepository)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, farmerOrMarketGardenerRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual onwardRoute.url
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, farmerOrMarketGardenerRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = form.bind(Map("value" -> ""))
//
//        val view = application.injector.instanceOf[FarmerOrMarketGardenerView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, farmerOrMarketGardenerRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "must redirect to Journey Recovery for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, farmerOrMarketGardenerRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//  }
//}
