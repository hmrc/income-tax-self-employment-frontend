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

package controllers.journeys.expenses.tailoring.individualCategories

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.data.EitherT
import forms.expenses.tailoring.individualCategories.GoodsToSellOrUseFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, Mtditid, Nino, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.GoodsToSellOrUse
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import pages.expenses.tailoring.individualCategories.GoodsToSellOrUsePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView

import scala.concurrent.Future

class GoodsToSellOrUseControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[GoodsToSellOrUse](
      "GoodsToSellOrUseController",
      GoodsToSellOrUsePage
    ) {

  override def onPageLoadCall: Call           = routes.GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call             = routes.GoodsToSellOrUseController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call              = routes.RepairsAndMaintenanceController.onPageLoad(taxYear, businessId, NormalMode)
  override def validAnswer: GoodsToSellOrUse  = YesDisallowable
  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(Accrual)
  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  def createForm(userType: UserType): Form[GoodsToSellOrUse] = new GoodsToSellOrUseFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

}
