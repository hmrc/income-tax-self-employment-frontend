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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import forms.standard.BooleanFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import pages.capitalallowances.electricVehicleChargePoints.ChargePointTaxReliefPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.electricVehicleChargePoints.ChargePointTaxReliefView

class ChargePointTaxReliefControllerSpec extends BooleanGetAndPostQuestionBaseSpec("ChargePointTaxReliefController", ChargePointTaxReliefPage) {

  override def onPageLoadCall: Call = routes.ChargePointTaxReliefController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.ChargePointTaxReliefController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = models.common.onwardRoute

  override val validAnswer = true

  override def createForm(user: UserType): Form[Boolean] = new BooleanFormProvider()(page, user, Some("zeroEmission"))

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ChargePointTaxReliefView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}
