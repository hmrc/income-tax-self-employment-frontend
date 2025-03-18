package controllers

import base.SpecBase
import controllers.journeys.expenses.travelAndAccommodation.routes.TravelForWorkYourVehicleController
import controllers.standard.routes.JourneyRecoveryController
import forms.TravelForWorkYourVehicleFormProvider
import models.{CheckMode, Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.TravelForWorkYourVehiclePage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.travelForWork.TravelForWorkYourVehicleView

import scala.concurrent.Future

class TravelForWorkYourVehicleControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {
  
  private val testAnswer = "Work Van"

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new TravelForWorkYourVehicleFormProvider()

  case class UserScenario(userType: UserType, form: Form[String])

  private val userScenarios = Seq(
    UserScenario(UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(UserType.Agent, formProvider(UserType.Agent))
  )

  private def onPageLoadRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, mode).url

  private def onSubmitRoute(taxYear: TaxYear, businessId: BusinessId, mode: Mode): String =
    TravelForWorkYourVehicleController.onSubmit(taxYear, businessId, mode).url

  private val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockService)
  }

  "TravelForWorkYourVehicleController" - {
    userScenarios.foreach { scenario =>
      s"when user is ${scenario.userType}" - {
        "onPageLoad" - {
          "must return OK and the correct view" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = scenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, NormalMode))
              val result = route(application, request).value
              val view = application.injector.instanceOf[TravelForWorkYourVehicleView]

              status(result) mustBe OK
              contentAsString(result) mustBe view(
                scenario.form,
                NormalMode,
                scenario.userType,
                taxYear,
                businessId
              )(request, messages(application)).toString
            }
          }

          "must populate the view correctly when the question has previously been answered" in {
            val userAnswers = UserAnswers(userAnswersId)
              .set(TravelForWorkYourVehiclePage, testAnswer, Some(businessId))
              .success
              .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userType = scenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, CheckMode))
              val view = application.injector.instanceOf[TravelForWorkYourVehicleView]
              val result = route(application, request).value

              status(result) mustBe OK
              contentAsString(result) mustBe view(
                scenario.form.fill(testAnswer),
                CheckMode,
                scenario.userType,
                taxYear,
                businessId
              )(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, NormalMode))
              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe JourneyRecoveryController.onPageLoad().url
            }
          }
        }

        "onSubmit" - {
          "must redirect to the next page when valid data is submitted" in {
            val mockSessionRepository = mock[SessionRepository]
            when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = scenario.userType)
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", testAnswer))

              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe onwardRoute.url
            }
          }

          "must return a BAD_REQUEST when invalid data is submitted" in {
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = scenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = scenario.form.bind(Map("value" -> ""))
              val view = application.injector.instanceOf[TravelForWorkYourVehicleView]
              val result = route(application, request).value

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(
                boundForm,
                NormalMode,
                scenario.userType,
                taxYear,
                businessId
              )(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, NormalMode))
                  .withFormUrlEncodedBody(("value", testAnswer))

              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}