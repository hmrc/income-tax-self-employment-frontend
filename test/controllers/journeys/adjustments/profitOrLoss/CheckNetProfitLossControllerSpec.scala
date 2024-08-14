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

import base.{ControllerSpec, SpecBase}
import builders.BusinessIncomeSourcesSummaryBuilder
import common.TestApp.buildAppFromUserType
import models.NormalMode
import models.database.UserAnswers
import models.journeys.adjustments.ProfitOrLoss
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import utils.Assertions.assertEqualWithDiff
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{buildTable1, buildTable2, buildTable3}
import views.html.journeys.adjustments.profitOrLoss.CheckNetProfitLossView

class CheckNetProfitLossControllerSpec extends ControllerSpec {

  def userAnswers: UserAnswers = buildUserAnswers(Json.obj())

  def onPageLoad: String      = routes.CheckNetProfitLossController.onPageLoad(taxYear, businessId).url
  def onwardLossRoute: Call   = routes.CurrentYearLossesController.onPageLoad(taxYear, businessId, NormalMode)
  def onwardProfitRoute: Call = routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, NormalMode)

  def onPageLoadRequest = FakeRequest(GET, onPageLoad)

  "onPageLoad" - {
    "should return Ok and render correct view" - {
      userTypeCases.foreach { userType =>
        s"when net loss and user is an $userType" in {
          val incomeSummary      = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetLoss
          val stubService        = SelfEmploymentServiceStub(getBusinessIncomeSourcesSummaryResult = Right(incomeSummary))
          val application        = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg       = SpecBase.messages(application)
          val result             = route(application, onPageLoadRequest).value
          val netAmount          = incomeSummary.returnNetBusinessProfitForTaxPurposes()
          val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
          val table1             = buildTable1(ProfitOrLoss.Loss, 3000, 0.05, -3100)
          val table2             = buildTable2(ProfitOrLoss.Loss, 0, -0.05, 100.20)
          val table3             = buildTable3(ProfitOrLoss.Loss, 200, -200.1)

          val expectedView: String = {
            val view = application.injector.instanceOf[CheckNetProfitLossView]
            view(userType, ProfitOrLoss.Loss, formattedNetAmount, table1, table2, table3, onwardLossRoute)(onPageLoadRequest, msg)
              .toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
        s"when net profit and user is an $userType" in {
          val incomeSummary = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetProfit
          val stubService = SelfEmploymentServiceStub(getBusinessIncomeSourcesSummaryResult = Right(incomeSummary))
          val application = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg = SpecBase.messages(application)
          val result = route(application, onPageLoadRequest).value
          val netAmount = incomeSummary.returnNetBusinessProfitForTaxPurposes()
          val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
          val table1 = buildTable1(ProfitOrLoss.Profit, 3000, 0.05, -3100)
          val table2 = buildTable2(ProfitOrLoss.Profit, 0, -0.05, 100.20)
          val table3 = buildTable3(ProfitOrLoss.Profit, 200, -200.1)

          val expectedView: String = {
            val view = application.injector.instanceOf[CheckNetProfitLossView]
            view(userType, ProfitOrLoss.Profit, formattedNetAmount, table1, table2, table3, onwardProfitRoute)(onPageLoadRequest, msg)
              .toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }
    }
  }

}
