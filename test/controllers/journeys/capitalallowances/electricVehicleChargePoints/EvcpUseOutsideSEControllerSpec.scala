/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import base.ControllerSpec
import cats.implicits.catsSyntaxOptionId
import controllers.standard.{routes => genRoutes}
import forms.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEFormProvider
import forms.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEFormProvider.EvcpUseOutsideSEFormModel
import models.NormalMode
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSE
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.capitalallowances.electricVehicleChargePoints._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.inject.{Binding, bind}
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.services.SelfEmploymentServiceStub
import views.html.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSEView

class EvcpUseOutsideSEControllerSpec extends ControllerSpec {

  private def onPageLoadRoute: String = routes.EvcpUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode).url
  private def onSubmitRoute: String   = routes.EvcpUseOutsideSEController.onSubmit(taxYear, businessId, NormalMode).url
  private def onwardRoute: Call       = routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

  private val validRadioAnswer: EvcpUseOutsideSE = EvcpUseOutsideSE.DifferentAmount
  private val validAmount: Int                   = 20

  private def baseAnswers: UserAnswers = emptyUserAnswersAccrual
  private def pageAnswers: UserAnswers = baseAnswers
    .set(EvcpUseOutsideSEPage, validRadioAnswer, businessId.some)
    .success
    .value
    .set(EvcpUseOutsideSEPercentagePage, validAmount, businessId.some)
    .success
    .value

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())

  override val bindings: List[Binding[_]] = List(
    bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute))
  )

  private def form: Form[EvcpUseOutsideSEFormModel]     = EvcpUseOutsideSEFormProvider(Individual)
  private def validFormModel: EvcpUseOutsideSEFormModel = EvcpUseOutsideSEFormModel(validRadioAnswer, validAmount)

  private def stubbedService: SelfEmploymentServiceStub       = SelfEmploymentServiceStub(saveAnswerResult = pageAnswers)
  private def getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoadRoute)

  private def expectedView(expectedForm: Form[EvcpUseOutsideSEFormModel], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[EvcpUseOutsideSEView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  "onPageLoad" - {
    "when answers exist for the page should" - {
      "return Ok and the view with the existing answer" in new TestScenario(Individual, answers = pageAnswers.some, service = stubbedService) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe OK
          contentAsString(result) shouldBe expectedView(form.fill(validFormModel), this)(getRequest, messages(application), application)
        }
      }
    }
    "when the page has no existing answers should" - {
      "return Ok and view content with no prefilled answers" in new TestScenario(Individual, answers = baseAnswers.some, service = stubbedService) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe OK
          contentAsString(result) shouldBe expectedView(form, this)(getRequest, messages(application), application)
        }
      }
    }
    "no answers exist in the session" - {
      "redirect to the journey recovery controller" in new TestScenario(Individual, answers = None, service = stubbedService) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe genRoutes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

  "onSubmit" - {
    " when valid data is submitted" - {
      "redirect to the next page" in new TestScenario(Individual, answers = pageAnswers.some, service = stubbedService) {
        running(application) {
          val request = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(
            ("radioPercentage", validRadioAnswer.toString),
            ("optDifferentAmount", validAmount.toString)
          )
          val result = route(application, request).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe onwardRoute.url
        }
      }
    }
    "when invalid data is submitted" - {
      "return a 400 and pass the errors to the view" in new TestScenario(Individual, answers = baseAnswers.some, service = stubbedService) {
        running(application) {
          val request = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(
            ("radioPercentage", "invalid value"),
            ("optDifferentAmount", "invalid value")
          )
          val result    = route(application, request).value
          val boundForm = form.bind(Map("radioPercentage" -> "invalid value", "optDifferentAmount" -> "invalid value"))

          status(result) shouldBe BAD_REQUEST
          contentAsString(result) shouldBe expectedView(boundForm, this)(request, messages(application), application)
        }
      }
    }
    "no answers exist in the session" - {
      "Redirect to the journey recovery page" in new TestScenario(Individual, answers = None, service = stubbedService) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe genRoutes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
