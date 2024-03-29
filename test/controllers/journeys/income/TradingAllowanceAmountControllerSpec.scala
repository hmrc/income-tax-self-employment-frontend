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

package controllers.journeys.income

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeIncomeNavigator, IncomeNavigator}
import pages.income.{TradingAllowanceAmountPage, TurnoverIncomeAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.income.TradingAllowanceAmountView

class TradingAllowanceAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("TradingAllowanceAmountController", TradingAllowanceAmountPage) {

  lazy val onPageLoadRoute: String = routes.TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.IncomeCYAController.onPageLoad(taxYear, businessId)

  override val bindings: List[Binding[_]] = List(bind[IncomeNavigator].toInstance(new FakeIncomeNavigator(onwardRoute)))

  override def baseAnswers: UserAnswers = emptyUserAnswersAccrual.set(TurnoverIncomeAmountPage, maxTradingAllowance, Some(businessId)).success.value

  val maxTradingAllowance: BigDecimal = 1000.00
  val smallTradingAllowance           = 400.00

  def createForm(userType: UserType): Form[BigDecimal] = form(
    page,
    userType,
    maxValue = maxTradingAllowance,
    minValueError = s"tradingAllowanceAmount.error.lessThanZero.$userType",
    maxValueError = s"tradingAllowanceAmount.error.overTurnover.$userType",
    nonNumericError = s"tradingAllowanceAmount.error.nonNumeric.$userType"
  )

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[TradingAllowanceAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
