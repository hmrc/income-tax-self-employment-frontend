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

package controllers.journeys.income

import base.ControllerSpec
import cats.implicits.catsSyntaxEitherId
import controllers.standard
import models.NormalMode
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsString, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import views.html.journeys.income.TradingAllowanceWarningView

class TradingAllowanceWarningControllerSpec extends ControllerSpec {

  lazy val onPageLoadRoute: String    = routes.TradingAllowanceWarningController.onPageLoad(taxYear, businessId).url
  lazy val onSubmitRoute: String      = routes.TradingAllowanceWarningController.onSubmit(taxYear, businessId).url
  lazy val onwardRoute: String        = routes.HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val errorRecoveryRoute: String = standard.routes.JourneyRecoveryController.onPageLoad().url

  private val someUserAnswers = Some(buildUserAnswers(JsObject.empty))
  private val connectorError  = ConnectorResponseError("", "", HttpError(BAD_REQUEST, HttpErrorBody.parsingError)).asLeft

  def expectedView(scenario: TestStubbedScenario)(implicit request: Request[_], messages: Messages, application: Application): String = {
    val view = application.injector.instanceOf[TradingAllowanceWarningView]
    view(scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  private def getRequest  = FakeRequest(GET, onPageLoadRoute)
  private def postRequest = FakeRequest(POST, onSubmitRoute)

  "on page load" - {
    "when UserAnswers exist" - {
      "should return the page in an 'Ok' result" in new TestStubbedScenario(answers = someUserAnswers) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe OK
          contentAsString(result) shouldBe expectedView(this)(getRequest, messages(application), application)
        }
      }
    }
    "when there are no UserAnswers in the session" - {
      "should redirect to the journey recovery controller" in new TestStubbedScenario(answers = None) {
        running(application) {
          val result = route(application, getRequest).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe errorRecoveryRoute
        }
      }
    }
  }

  "on page submission" - {
    "should redirect to the HowMuchTradingAllowance page" - {
      "when UserAnswers exist and clearing Expenses and CapitalAllowances data is successful" in new TestStubbedScenario(answers = someUserAnswers) {
        running(application) {
          val result                     = route(application, postRequest).value
          val redirectMatchesOnwardRoute = redirectLocation(result).value.endsWith(onwardRoute)

          status(result) shouldBe SEE_OTHER
          assert(redirectMatchesOnwardRoute)
        }
      }
    }
    "should redirect to the journey recovery controller" - {
      "when a service error is returned when trying to clear Expenses and CapitalAllowances data" in new TestStubbedScenario(
        answers = someUserAnswers,
        stubbedService = SelfEmploymentServiceStub(clearExpensesAndCapitalAllowancesResult = connectorError)) {
        running(application) {
          val result = route(application, postRequest).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe errorRecoveryRoute
        }
      }
      "when there are no UserAnswers in the session" in new TestStubbedScenario(answers = None) {
        running(application) {
          val result = route(application, postRequest).value

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).value shouldBe errorRecoveryRoute
        }
      }
    }
  }
}
