/*
 * Copyright 2024 HM Revenue & Customs
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

/*
package controllers.journeys.adjustments.profitOrLoss

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import models.NormalMode
import pages.adjustments.profitOrLoss.CarryLossForwardPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.adjustments.profitOrLoss.CarryLossForwardView

class CurrentYearLossControllerCarryLossForwardSpec extends BooleanGetAndPostQuestionBaseSpec("CurrentYearLossController", CarryLossForwardPage) {

  override def onPageLoadCall: Call = routes.CurrentYearLossController.onPageLoad(taxYear, businessId, NormalMode)

  override def onSubmitCall: Call = routes.CurrentYearLossController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, NormalMode)

  override def expectedView(expectedForm: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[CarryLossForwardView]
    view(expectedForm, taxYear, businessId, scenario.userType, scenario.mode).toString()
  }

  // TODO in SASS-9566 test that the view CarryLossForward gets displayed when user has NO other incomes
}

 */
