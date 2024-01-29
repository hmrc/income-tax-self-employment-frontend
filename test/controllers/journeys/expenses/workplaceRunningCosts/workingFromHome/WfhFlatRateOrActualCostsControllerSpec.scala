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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import forms.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsFormProvider
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts.FlatRate
import models.{Mode, NormalMode}
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import org.mockito.Mockito.when
import pages.expenses.workplaceRunningCosts.workingFromHome.{
  WfhFlatRateOrActualCostsPage,
  WorkingFromHomeHours101Plus,
  WorkingFromHomeHours25To50,
  WorkingFromHomeHours51To100
}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.expenses.workplaceRunningCosts.workingFromHome.FlatRateViewModel
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WfhFlatRateOrActualCostsView

import scala.concurrent.Future

class WfhFlatRateOrActualCostsControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[WfhFlatRateOrActualCosts]("WfhFlatRateOrActualCostsController", WfhFlatRateOrActualCostsPage) {

  override def onPageLoadCall: Call                  = routes.WfhFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call                    = submissionCall(NormalMode)
  override def onwardRoute: Call                     = expectedRedirectCall()
  override def validAnswer: WfhFlatRateOrActualCosts = FlatRate
  private lazy val validMonths                       = 3
  private lazy val validMonthsText                   = s"$validMonths months"
  private lazy val amount25To50                      = validMonths * 10
  private lazy val amount51To100                     = validMonths * 18
  private lazy val amount101Plus                     = validMonths * 26
  private lazy val flatRate                          = amount25To50 + amount51To100 + amount101Plus
  private lazy val flatRateViewModel = FlatRateViewModel(
    validMonthsText,
    formatMoney(amount25To50),
    validMonthsText,
    formatMoney(amount51To100),
    validMonthsText,
    formatMoney(amount101Plus),
    formatMoney(flatRate)
  )

  private def submissionCall(mode: Mode): Call = routes.WfhFlatRateOrActualCostsController.onSubmit(taxYear, businessId, mode)
  private def expectedRedirectCall(): Call     = routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId)

  override def baseAnswers: UserAnswers = emptyUserAnswers
    .set(WorkingFromHomeHours25To50, validMonths, Some(businessId))
    .success
    .value
    .set(WorkingFromHomeHours51To100, validMonths, Some(businessId))
    .success
    .value
    .set(WorkingFromHomeHours101Plus, validMonths, Some(businessId))
    .success
    .value
  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute)))

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def createForm(userType: UserType): Form[WfhFlatRateOrActualCosts] = new WfhFlatRateOrActualCostsFormProvider()(userType)

  override def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WfhFlatRateOrActualCostsView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, flatRateViewModel).toString()
  }
}
