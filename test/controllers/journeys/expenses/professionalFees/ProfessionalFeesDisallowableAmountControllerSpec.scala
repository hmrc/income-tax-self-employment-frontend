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

package controllers.journeys.expenses.professionalFees

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.professionalFees.ProfessionalFeesDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.professionalFees.ProfessionalFeesDisallowableAmountView

class ProfessionalFeesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "ProfessionalFeesDisallowableAmountController",
      ProfessionalFeesDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.ProfessionalFeesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override def baseAnswers = emptyUserAnswers.set(ProfessionalFeesAmountPage, amount, Some(businessId)).success.value

  override def createForm(userType: UserType): Form[BigDecimal] = new ProfessionalFeesDisallowableAmountFormProvider()(userType, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ProfessionalFeesDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, formatMoney(amount)).toString()
  }

}
