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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import base.ControllerSpec
import controllers.standard
import models.NormalMode
import models.common.UserType.{Agent, Individual}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WfhExpensesInfoView

class WfhExpensesInfoControllerSpec extends ControllerSpec {

  private lazy val onPageLoadCall = routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId)
  private lazy val redirectCall   = routes.WfhClaimingAmountController.onPageLoad(taxYear, businessId, NormalMode)
  private lazy val getRequest     = FakeRequest(GET, onPageLoadCall.url)

  private def expectedView(scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String = {
    val view = application.injector.instanceOf[WfhExpensesInfoView]
    view(scenario.userType, redirectCall.url).toString()
  }

  Seq(Individual, Agent) foreach { userType =>
    s"onPageLoad, when user is an $userType" - {
      "Loading page" - {
        "Redirect to Journey Recover for a GET if prerequisite data is not found" in new TestScenario(userType, None) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "Return OK for a GET if an empty page" in new TestScenario(userType, Some(emptyUserAnswers)) {
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedView(this)(getRequest, messages(application), application)
          }
        }
      }
    }
  }

}
