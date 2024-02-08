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

package controllers.journeys.capitalallowances.tailoring

import base.questionPages.CheckboxControllerBaseSpec
import forms.capitalallowances.tailoring.SelectCapitalAllowancesFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.tailoring.CapitalAllowances
import models.journeys.capitalallowances.tailoring.CapitalAllowances.ElectricVehicleChargepoint
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.tailoring.SelectCapitalAllowancesView

class SelectCapitalAllowancesControllerSpec extends CheckboxControllerBaseSpec("SelectCapitalAllowancesController", SelectCapitalAllowancesPage) {

  override def onPageLoadRoute = routes.SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode).url
  override def onSubmitRoute   = routes.SelectCapitalAllowancesController.onSubmit(taxYear, businessId, NormalMode).url

  override def onwardRoute: Call = models.common.onwardRoute

  override def createForm(user: UserType): Form[Set[CapitalAllowances]] = new SelectCapitalAllowancesFormProvider()()

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[SelectCapitalAllowancesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns pageAnswers.asFuture

  override def answer: Set[CapitalAllowances] = Set(ElectricVehicleChargepoint)

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}
