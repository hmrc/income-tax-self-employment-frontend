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

package controllers.journeys.expenses.entertainment

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.UserType
import pages.expenses.entertainment.EntertainmentAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.entertainment.EntertainmentAmountView

class EntertainmentAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "EntertainmentAmountController",
      EntertainmentAmountPage
    ) {

  val onwardRoute: Call = models.common.onwardRoute

  lazy val onPageLoadRoute: String = routes.EntertainmentAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.EntertainmentAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override def createForm(userType: UserType): Form[BigDecimal] = form(page, userType, prefix = Some("entertainment"))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[EntertainmentAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }
}
