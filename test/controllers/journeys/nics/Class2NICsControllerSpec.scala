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

package controllers.journeys.nics

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import models.NormalMode
import pages.nics.Class2NICsPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.nics.Class2NICsView

class Class2NICsControllerSpec extends BooleanGetAndPostQuestionBaseSpec("Class2NICsController", Class2NICsPage) {

  override val checkForExistingAnswers = false

  override def onPageLoadCall: Call = routes.Class2NICsController.onPageLoad(taxYear, NormalMode)
  override def onSubmitCall: Call   = routes.Class2NICsController.onSubmit(taxYear, NormalMode)

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[Class2NICsView]
    view(form, scenario.taxYear, scenario.userType, scenario.mode).toString()
  }

}
