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

import base.SpecBase
import controllers.journeys.expenses.travelAndAccommodation.routes.TravelForWorkYourVehicleController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.travelAndAccommodation.TravelForWorkYourVehicleFormProvider
import models.common.{BusinessId, JourneyContext, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.{CheckMode, Index, Mode, NormalMode}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import services.answers.AnswersService
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourVehicleView

import scala.concurrent.Future

class TravelForWorkYourVehicleControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val testAnswer = "Work Van"

  private def onwardRoute: Call = routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode)

  private val formProvider       = new TravelForWorkYourVehicleFormProvider()
  private val mockAnswersService = mock[AnswersService]

  case class UserScenario(userType: UserType, form: Form[String])

  private val userScenarios = Seq(
    UserScenario(UserType.Individual, formProvider(UserType.Individual)),
    UserScenario(UserType.Agent, formProvider(UserType.Agent))
  )

  private def onPageLoadRoute(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): String =
    TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, index, mode).url

  private def onSubmitRoute(taxYear: TaxYear, businessId: BusinessId, index: Index, mode: Mode): String =
    TravelForWorkYourVehicleController.onSubmit(taxYear, businessId, index, mode).url

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
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = scenario.userType)
              .overrides(
                bind[AnswersService].toInstance(mockAnswersService)
              )
              .build()

            when(mockAnswersService.getAnswers[VehicleDetailsDb](any[JourneyContext], any[Option[Index]])(any, any))
              .thenReturn(Future.successful(Some(VehicleDetailsDb())))

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, index, NormalMode))
              val result  = route(application, request).value
              val view    = application.injector.instanceOf[TravelForWorkYourVehicleView]

              status(result) mustBe OK
              contentAsString(result) mustBe view(
                scenario.form,
                NormalMode,
                scenario.userType,
                taxYear,
                businessId,
                index
              )(request, messages(application)).toString
            }
          }

          "must populate the view correctly when the question has previously been answered" in {
            val userAnswers = UserAnswers(userAnswersId)
              .set(TravelForWorkYourVehiclePage, testAnswer, Some(businessId))
              .success
              .value

            when(mockAnswersService.getAnswers[VehicleDetailsDb](any[JourneyContext], any[Option[Index]])(any, any))
              .thenReturn(Future.successful(Some(VehicleDetailsDb(description = Some(testAnswer)))))

            val application = applicationBuilder(userAnswers = Some(userAnswers), userType = scenario.userType)
              .overrides(
                bind[AnswersService].toInstance(mockAnswersService)
              )
              .build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, index, CheckMode))
              val view    = application.injector.instanceOf[TravelForWorkYourVehicleView]
              val result  = route(application, request).value

              val expectedForm = new TravelForWorkYourVehicleFormProvider()(scenario.userType).fill(testAnswer)

              status(result) mustBe OK
              contentAsString(result) mustBe view(
                expectedForm,
                CheckMode,
                scenario.userType,
                taxYear,
                businessId,
                index
              )(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request = FakeRequest(GET, onPageLoadRoute(taxYear, businessId, index, NormalMode))
              val result  = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe JourneyRecoveryController.onPageLoad().url
            }
          }
        }

        "onSubmit" - {
          "must redirect to the next page when valid data is submitted" in {

            val vehicleDetailsDb = VehicleDetailsDb(description = Some("new Car"))
            when(mockAnswersService.getAnswers[VehicleDetailsDb](any[JourneyContext], any[Option[Index]])(any, any))
              .thenReturn(Future.successful(Some(vehicleDetailsDb)))

            when(mockAnswersService.replaceAnswers[VehicleDetailsDb](any[JourneyContext], any[VehicleDetailsDb], any[Option[Index]])(any, any))
              .thenReturn(Future.successful(VehicleDetailsDb(description = Some(testAnswer))))

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers), userType = scenario.userType)
                .overrides(
                  bind[AnswersService].toInstance(mockAnswersService)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, index, NormalMode))
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
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, index, NormalMode))
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = scenario.form.bind(Map("value" -> ""))
              val view      = application.injector.instanceOf[TravelForWorkYourVehicleView]
              val result    = route(application, request).value

              status(result) mustBe BAD_REQUEST
              contentAsString(result) mustBe view(
                boundForm,
                NormalMode,
                scenario.userType,
                taxYear,
                businessId,
                index
              )(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery if no existing data is found" in {
            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, onSubmitRoute(taxYear, businessId, index, NormalMode))
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
