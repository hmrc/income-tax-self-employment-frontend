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
import forms.expenses.tailoring.individualCategories.GoodsToSellOrUseFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import models.journeys.expenses.individualCategories.{GoodsToSellOrUse, TaxiMinicabOrRoadHaulage}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.tailoring.individualCategories.{GoodsToSellOrUsePage, TaxiMinicabOrRoadHaulagePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.{JsString, Writes}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView

import scala.concurrent.Future

class GoodsToSellOrUseControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[GoodsToSellOrUse](
      "GoodsToSellOrUseController",
      GoodsToSellOrUsePage
    ) {

  override implicit val writes: Writes[GoodsToSellOrUse] = Writes(value => JsString(value.toString))

  override lazy val onPageLoadCall: Call          = routes.GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)
  override lazy val onSubmitCall: Call            = routes.GoodsToSellOrUseController.onSubmit(taxYear, businessId, NormalMode)
  override lazy val onwardRoute: Call             = routes.RepairsAndMaintenanceController.onPageLoad(taxYear, businessId, NormalMode)
  override lazy val validAnswer: GoodsToSellOrUse = YesDisallowable

  override val blankUserAnswers: UserAnswers =
    emptyUserAnswers.set(TaxiMinicabOrRoadHaulagePage, TaxiMinicabOrRoadHaulage.Yes, Some(businessId)).success.value
  override val filledUserAnswers: UserAnswers = blankUserAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  when(mockService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(Accrual))
  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  def createForm(userType: UserType): Form[GoodsToSellOrUse] = new GoodsToSellOrUseFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual, taxiDriver = true).toString()
  }

}
