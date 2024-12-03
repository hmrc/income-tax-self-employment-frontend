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

package controllers.journeys.adjustments.profitOrLoss

import base.questionPages.CheckboxControllerBaseSpec
import forms.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossFormProvider
import models.NormalMode
import models.common.UserType
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
import pages.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossView

class CurrentYearLossControllerWhatDoYouWantToDoWithLossSpec
    extends CheckboxControllerBaseSpec("CurrentYearLossController", WhatDoYouWantToDoWithLossPage) {

  override def answer: Set[WhatDoYouWantToDoWithLoss] = Set(DeductFromOtherTypes)

  override def onPageLoadRoute: String = routes.CurrentYearLossController.onPageLoad(taxYear, businessId, NormalMode).url

  override def onSubmitRoute: String = routes.CurrentYearLossController.onSubmit(taxYear, businessId, NormalMode).url

  override def onwardRoute: Call = routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, NormalMode)

  override def createForm(userType: UserType): Form[Set[WhatDoYouWantToDoWithLoss]] =
    new WhatDoYouWantToDoWithLossFormProvider()(WhatDoYouWantToDoWithLossPage, userType)

  override def expectedView(expectedForm: Form[_], scenario: TestStubbedScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WhatDoYouWantToDoWithLossView]
    view(expectedForm, taxYear, businessId, scenario.userType, scenario.mode).toString()
  }
}
