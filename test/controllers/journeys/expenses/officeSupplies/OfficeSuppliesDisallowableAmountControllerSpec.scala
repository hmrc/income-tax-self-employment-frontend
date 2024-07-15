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

package controllers.journeys.expenses.officeSupplies

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n._
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesDisallowableAmountView

class OfficeSuppliesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "OfficeSuppliesDisallowableAmountController",
      OfficeSuppliesDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.OfficeSuppliesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

  override def baseAnswers = emptyUserAnswers.set(OfficeSuppliesAmountPage, amount, Some(businessId)).success.value

  override def createForm(userType: UserType): Form[BigDecimal] = new OfficeSuppliesDisallowableAmountFormProvider()(userType, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OfficeSuppliesDisallowableAmountView]
    view(form, scenario.mode, scenario.taxYear, scenario.businessId, scenario.userType, formatMoney(amount)).toString()
  }

}
