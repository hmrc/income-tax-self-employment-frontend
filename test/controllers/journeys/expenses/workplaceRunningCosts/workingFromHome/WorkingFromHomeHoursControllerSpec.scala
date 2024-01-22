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

import base.questionPages.MultipleIntGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider.WorkingFromHomeHoursFormModel
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.inject.{Binding, bind}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursView

class WorkingFromHomeHoursControllerSpec
    extends MultipleIntGetAndPostQuestionBaseSpec[WorkingFromHomeHoursFormModel]("WorkingFromHomeHoursController", WorkingFromHomeHoursPage) {

  override def onPageLoadRoute: String = routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, NormalMode).url
  override def onSubmitRoute: String   = routes.WorkingFromHomeHoursController.onSubmit(taxYear, businessId, NormalMode).url
  override def onwardRoute: Call       = routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, NormalMode)

  override def pageAnswers: UserAnswers = baseAnswers
    .set(WorkingFromHomeHours25To50, amount, businessId.some)
    .success
    .value
    .set(WorkingFromHomeHours51To100, amount, businessId.some)
    .success
    .value
    .set(WorkingFromHomeHours101Plus, amount, businessId.some)
    .success
    .value

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  private val maxMonths = 11

  override def createForm(userType: UserType): Form[WorkingFromHomeHoursFormModel] = WorkingFromHomeHoursFormProvider(userType, maxMonths)
  override def validFormModel: WorkingFromHomeHoursFormModel                       = WorkingFromHomeHoursFormModel(amount, amount, amount)

  override def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(
    ("value25To50", amount.toString),
    ("value51To100", amount.toString),
    ("value101Plus", amount.toString)
  )

  override def expectedView(expectedForm: Form[WorkingFromHomeHoursFormModel], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WorkingFromHomeHoursView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, maxMonths.toString).toString()
  }

}
