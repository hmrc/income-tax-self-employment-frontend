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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.workplaceRunningCosts
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpFlatRateOrActualCostsFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.workplaceRunningCosts.WfbpFlatRateOrActualCosts
import models.journeys.expenses.workplaceRunningCosts.WfbpFlatRateOrActualCosts.FlatRate
import models.{Mode, NormalMode}
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import org.mockito.Mockito.when
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.expenses.workplaceRunningCosts.WfbpFlatRateViewModel
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpFlatRateOrActualCostsView

import scala.concurrent.Future

class WfbpFlatRateOrActualCostsControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[WfbpFlatRateOrActualCosts]("WfbpFlatRateOrActualCostsController", WfbpFlatRateOrActualCostsPage) {

  override def onPageLoadCall: Call                   = routes.WfbpFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call                     = submissionCall(NormalMode)
  override def onwardRoute: Call                      = expectedRedirectCall()
  override def validAnswer: WfbpFlatRateOrActualCosts = FlatRate
  private lazy val validMonths                        = 3
  private lazy val validMonthsText                    = s"$validMonths months"
  private lazy val amount1Person                      = validMonths * 350
  private lazy val amount2People                      = validMonths * 500
  private lazy val amount3People                      = validMonths * 650
  private lazy val flatRate                           = amount1Person + amount2People + amount3People
  private lazy val flatRateViewModel = WfbpFlatRateViewModel(
    validMonthsText,
    formatMoney(amount1Person),
    validMonthsText,
    formatMoney(amount2People),
    validMonthsText,
    formatMoney(amount3People),
    flatRate
  )

  private def submissionCall(mode: Mode): Call = routes.WfbpFlatRateOrActualCostsController.onSubmit(taxYear, businessId, mode)
  private def expectedRedirectCall(): Call =
    workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)

  override def baseAnswers: UserAnswers = emptyUserAnswers
    .set(LivingAtBusinessPremisesOnePerson, validMonths, Some(businessId))
    .success
    .value
    .set(LivingAtBusinessPremisesTwoPeople, validMonths, Some(businessId))
    .success
    .value
    .set(LivingAtBusinessPremisesThreePlusPeople, validMonths, Some(businessId))
    .success
    .value
  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute)))

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def createForm(userType: UserType): Form[WfbpFlatRateOrActualCosts] =
    new WfbpFlatRateOrActualCostsFormProvider()(userType, flatRateViewModel.flatRate)

  override def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WfbpFlatRateOrActualCostsView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, flatRateViewModel).toString()
  }
}
