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

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.UserType
import pages.adjustments.profitOrLoss.UnusedLossAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.adjustments.profitOrLoss.UnusedLossAmountView

class UnusedLossAmountControllerSpec extends BigDecimalGetAndPostQuestionBaseSpec("UnusedLossAmountController", UnusedLossAmountPage) {

  override def onPageLoadRoute: String = routes.UnusedLossAmountController.onPageLoad(taxYear, businessId, NormalMode).url

  override def onSubmitRoute: String = routes.UnusedLossAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.WhichYearIsLossReportedController.onPageLoad(taxYear, businessId, NormalMode)

  override def createForm(userType: UserType): Form[BigDecimal] = form(page, userType, prefix = Some("goodsAndServicesAmount"))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[UnusedLossAmountView]
    view(form, scenario.taxYear, scenario.businessId, NormalMode).toString()
  }
}