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

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.AccountingType.Accrual
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import pages.capitalallowances.tailoring.ClaimCapitalAllowancesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.capitalallowances.AssetBasedAllowanceSummary.buildCarsAndAssetBasedAllowanceTable
import views.html.journeys.capitalallowances.tailoring.ClaimCapitalAllowancesView

class ClaimCapitalAllowancesControllerSpec extends BooleanGetAndPostQuestionBaseSpec("ClaimCapitalAllowancesController", ClaimCapitalAllowancesPage) {

  override def onPageLoadCall: Call = routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.ClaimCapitalAllowancesController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = models.common.onwardRoute

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view                                     = application.injector.instanceOf[ClaimCapitalAllowancesView]
    val netAmount                                = BigDecimal(12345.67)
    val formattedNetAmount                       = formatSumMoneyNoNegative(List(netAmount))
    val yourBuildCarsAndAssetBasedAllowanceTable = buildCarsAndAssetBasedAllowanceTable()
    view(
      form,
      scenario.mode,
      scenario.userType,
      scenario.taxYear,
      Accrual,
      scenario.businessId,
      formattedNetAmount,
      yourBuildCarsAndAssetBasedAllowanceTable).toString()
  }

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}
