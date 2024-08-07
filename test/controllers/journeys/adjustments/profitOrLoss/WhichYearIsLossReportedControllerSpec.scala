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

package controllers.journeys.adjustments.profitOrLoss

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import models.NormalMode
import models.database.UserAnswers
import models.journeys.adjustments.WhichYearIsLossReported
import org.mockito.Mockito.when
import pages.adjustments.profitOrLoss.{UnusedLossAmountPage, WhichYearIsLossReportedPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.adjustments.profitOrLoss.WhichYearIsLossReportedView

import scala.concurrent.Future

class WhichYearIsLossReportedControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[WhichYearIsLossReported]("WhichYearIsLossReportedController", WhichYearIsLossReportedPage) {

  private val validCurrencyAmount: BigDecimal = 250.00

  override def onPageLoadCall: Call = routes.WhichYearIsLossReportedController.onPageLoad(taxYear, businessId, NormalMode)

  override def onSubmitCall: Call = routes.WhichYearIsLossReportedController.onPageLoad(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = routes.ProfitOrLossCYAController.onPageLoad(taxYear, businessId)

  override def validAnswer: WhichYearIsLossReported = WhichYearIsLossReported.Year2022to2023
  override def baseAnswers: UserAnswers             = emptyUserAnswers.set(UnusedLossAmountPage, validCurrencyAmount, Some(businessId)).success.value

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WhichYearIsLossReportedView]
    view(expectedForm, scenario.taxYear, scenario.businessId, scenario.userType, NormalMode, validCurrencyAmount).toString()
  }

}
