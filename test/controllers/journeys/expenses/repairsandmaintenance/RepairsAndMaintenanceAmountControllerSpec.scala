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

package controllers.journeys.expenses.repairsandmaintenance

import base.{BigDecimalGetAndPostQuestionBaseSpec}
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import models.{Mode, NormalMode}
import models.common.UserType
import controllers.journeys.expenses.repairsandmaintenance.routes.RepairsAndMaintenanceAmountController
import controllers.standard.routes.JourneyRecoveryController
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

class RepairsAndMaintenanceAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("RepairsAndMaintenanceAmountController", RepairsAndMaintenanceAmountPage) {
  lazy val onPageLoadRoute = RepairsAndMaintenanceAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode).url
  lazy val onSubmitRoute   = RepairsAndMaintenanceAmountController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url

  def createForm(userType: UserType): Form[BigDecimal] = new RepairsAndMaintenanceAmountFormProvider()(userType)

  def renderView(implicit request: Request[_], messages: Messages, application: Application): (Form[_], Mode, Int, String) => HtmlFormat.Appendable =
    application.injector.instanceOf[RepairsAndMaintenanceAmountView].apply _

}
