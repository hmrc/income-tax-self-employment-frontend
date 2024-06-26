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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.capitalallowances.electricVehicleChargePoints.AmountSpentOnEvcpFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import pages.capitalallowances.electricVehicleChargePoints.AmountSpentOnEvcpPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.electricVehicleChargePoints.AmountSpentOnEvcpView

class AmountSpentOnEvcpControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "AmountSpentOnEvcpController",
      AmountSpentOnEvcpPage
    ) {

  lazy val onPageLoadRoute = routes.AmountSpentOnEvcpController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.AmountSpentOnEvcpController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.EvcpOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

  override val bindings: List[Binding[_]] = List(bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute)))

  override def createForm(userType: UserType): Form[BigDecimal] = new AmountSpentOnEvcpFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[AmountSpentOnEvcpView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
