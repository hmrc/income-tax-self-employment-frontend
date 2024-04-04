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

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import controllers.returnAccountingType
import forms.income.TradingAllowanceFormProvider
import models.NormalMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.income.TradingAllowance
import models.requests.DataRequest
import org.mockito.IdiomaticMockito.StubbingOps
import pages.OneQuestionPage
import pages.income.TradingAllowancePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Request}
import views.html.journeys.income.TradingAllowanceView

class TradingAllowanceControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[TradingAllowance]("TradingAllowanceController", TradingAllowancePage) {

  override def onPageLoadCall: Call = routes.TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

  override def onSubmitCall: Call = routes.TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = routes.HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

  override def validAnswer: TradingAllowance = TradingAllowance.UseTradingAllowance

  override def createForm(userType: UserType): Form[TradingAllowance] = new TradingAllowanceFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view                        = application.injector.instanceOf[TradingAllowanceView]
    val dataRequest: DataRequest[_] = fakeDataRequest(emptyUserAnswersAccrual)
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, returnAccountingType(businessId)(dataRequest)).toString()
  }

  mockService
    .submitGatewayQuestionAndRedirect[Boolean](*[OneQuestionPage[Boolean]], *[BusinessId], *[UserAnswers], *, *[TaxYear], *) returns Redirect(
    onwardRoute).asFuture

}
