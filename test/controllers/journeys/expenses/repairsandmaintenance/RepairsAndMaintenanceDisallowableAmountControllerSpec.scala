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

import base.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.repairsandmaintenance.{routes => genRoutes}
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountFormProvider
import models.common.UserType
import models.{Mode, NormalMode}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountView

class RepairsAndMaintenanceDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "RepairsAndMaintenanceAmountController",
      RepairsAndMaintenanceDisallowableAmountPage
    ) {
  lazy val onPageLoadRoute = genRoutes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode).url
  lazy val onSubmitRoute   = genRoutes.RepairsAndMaintenanceDisallowableAmountController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url

  override val bindings: List[Binding[_]] = List(bind[ExpensesNavigator].toInstance(FakeExpensesNavigator()))

  def createForm(userType: UserType): Form[BigDecimal] = new RepairsAndMaintenanceDisallowableAmountFormProvider()(userType, 1000.0)

  def renderView(implicit request: Request[_], messages: Messages, application: Application): (Form[_], Mode, Int, String) => HtmlFormat.Appendable =
    application.injector.instanceOf[RepairsAndMaintenanceDisallowableAmountView].apply _

}
