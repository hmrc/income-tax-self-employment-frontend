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

package controllers.journeys.expenses.goodsToSellOrUse

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.UserType
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.goodsToSellOrUse.{GoodsToSellOrUseAmountPage, TaxiMinicabOrRoadHaulagePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountView

class GoodsToSellOrUseAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "GoodsToSellOrUseAmountController",
      GoodsToSellOrUseAmountPage
    ) {

  lazy val onPageLoadRoute = routes.GoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.GoodsToSellOrUseAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override def baseAnswers = emptyUserAnswersAccrual.set(TaxiMinicabOrRoadHaulagePage, true, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  def createForm(userType: UserType): Form[BigDecimal] = new GoodsToSellOrUseAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual, true).toString()
  }

}
