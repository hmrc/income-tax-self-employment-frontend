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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsPreviousClaimedAmountFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsPreviousClaimedAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsPreviousClaimedAmountView

class StructuresBuildingsPreviousClaimedAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "StructuresBuildingsPreviousClaimedAmountController",
      StructuresBuildingsPreviousClaimedAmountPage
    ) {

  lazy val onPageLoadRoute = routes.StructuresBuildingsPreviousClaimedAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.StructuresBuildingsPreviousClaimedAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.StructuresBuildingsNewClaimAmountController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override val bindings: List[Binding[_]] = List(bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute)))

  override def createForm(userType: UserType): Form[BigDecimal] = new StructuresBuildingsPreviousClaimedAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsPreviousClaimedAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
