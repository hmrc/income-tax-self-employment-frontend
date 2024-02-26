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

package controllers.journeys.capitalallowances.zeroEmissionCars

import base.ControllerSpec
import cats.implicits.catsSyntaxOptionId
import controllers.standard.{routes => genRoutes}
import forms.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaimFormProvider
import forms.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaimFormProvider.ZecHowMuchDoYouWantToClaimModel
import models.NormalMode
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaim
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.capitalallowances.zeroEmissionCars._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.inject.{Binding, bind}
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.services.SelfEmploymentServiceStub
import views.html.journeys.capitalallowances.zeroEmissionCars.ZecHowMuchDoYouWantToClaimView

class ZecHowMuchDoYouWantToClaimControllerSpec extends ControllerSpec {

  private def onPageLoadRoute: String = routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode).url
  private def onSubmitRoute: String   = routes.ZecHowMuchDoYouWantToClaimController.onSubmit(taxYear, businessId, NormalMode).url
  private def onwardRoute: Call       = routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

  private val fullCost: BigDecimal                         = 5000.00
  private val validRadioAnswer: ZecHowMuchDoYouWantToClaim = ZecHowMuchDoYouWantToClaim.LowerAmount
  private val validCurrencyAmount: BigDecimal              = 4000
  private val percentage: Int                              = 10
  private val expectedFullCost: BigDecimal                 = BigDecimal(4500.00).setScale(0)

  private def baseAnswers: UserAnswers = emptyUserAnswersAccrual
    .set(ZecTotalCostOfCarPage, fullCost, businessId.some)
    .success
    .value
    .set(ZecUseOutsideSEPercentagePage, percentage, businessId.some)
    .success
    .value
  private def pageAnswers: UserAnswers = baseAnswers
    .set(ZecHowMuchDoYouWantToClaimPage, validRadioAnswer, businessId.some)
    .success
    .value
    .set(ZecClaimAmount, validCurrencyAmount, businessId.some)
    .success
    .value

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())

  override val bindings: List[Binding[_]] = List(
    bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute))
  )

  private def form: Form[ZecHowMuchDoYouWantToClaimModel]     = ZecHowMuchDoYouWantToClaimFormProvider(Individual, fullCost)
  private def validFormModel: ZecHowMuchDoYouWantToClaimModel = ZecHowMuchDoYouWantToClaimModel(validRadioAnswer, validCurrencyAmount)

  private def stubbedService: SelfEmploymentServiceStub       = SelfEmploymentServiceStub(saveAnswerResult = pageAnswers)
  private def getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoadRoute)

  private def expectedView(expectedForm: Form[ZecHowMuchDoYouWantToClaimModel], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ZecHowMuchDoYouWantToClaimView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, expectedFullCost).toString()
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
            ("howMuchDoYouWantToClaim", validRadioAnswer.toString),
            ("totalCost", validCurrencyAmount.toString)
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
            ("howMuchDoYouWantToClaim", "invalid value"),
            ("totalCost", "invalid value")
          )
          val result    = route(application, request).value
          val boundForm = form.bind(Map("howMuchDoYouWantToClaim" -> "invalid value", "totalCost" -> "invalid value"))

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
