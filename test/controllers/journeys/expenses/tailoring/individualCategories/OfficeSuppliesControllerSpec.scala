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
import forms.expenses.tailoring.individualCategories.OfficeSuppliesFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, Mtditid, Nino, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OfficeSupplies
import models.journeys.expenses.individualCategories.OfficeSupplies.YesDisallowable
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.individualCategories.OfficeSuppliesView

import scala.concurrent.Future

class OfficeSuppliesControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[OfficeSupplies](
      "OfficeSuppliesController",
      OfficeSuppliesPage
    ) {

  override def onPageLoadCall: Call           = routes.OfficeSuppliesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call             = routes.OfficeSuppliesController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call              = routes.TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, businessId, NormalMode)
  override def validAnswer: OfficeSupplies    = YesDisallowable
  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns EitherT.rightT(Accrual)
  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  def createForm(userType: UserType): Form[OfficeSupplies] = new OfficeSuppliesFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OfficeSuppliesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

}
