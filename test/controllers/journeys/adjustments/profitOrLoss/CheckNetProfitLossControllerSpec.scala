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
import builders.{BusinessIncomeSourcesSummaryBuilder, NetBusinessProfitOrLossValuesBuilder}
import common.TestApp.buildAppFromUserType
import models.NormalMode
import models.database.UserAnswers
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import utils.Assertions.assertEqualWithDiff
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.buildTables
import views.html.journeys.adjustments.profitOrLoss.CheckNetProfitLossView

class CheckNetProfitLossControllerSpec extends ControllerSpec {

  def userAnswers: UserAnswers = buildUserAnswers(Json.obj())

  def onPageLoad: String      = routes.CheckNetProfitLossController.onPageLoad(taxYear, businessId).url
  def onwardLossRoute: Call   = routes.ClaimLossReliefController.onPageLoad(taxYear, businessId, NormalMode)
  def onwardProfitRoute: Call = routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, NormalMode)

  def onPageLoadRequest = FakeRequest(GET, onPageLoad)

  "onPageLoad" - {
    "should return Ok and render correct view" - {
      userTypeCases.foreach { userType =>
        s"when net loss and user is an $userType" in {
          val incomeSummary                 = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetLoss
          val netBusinessProfitOrLossValues = NetBusinessProfitOrLossValuesBuilder.aNetBusinessLossValues
          val stubService = SelfEmploymentServiceStub(
            getBusinessIncomeSourcesSummaryResult = Right(incomeSummary),
            getNetBusinessProfitOrLossValuesResult = Right(netBusinessProfitOrLossValues)
          )
          val application        = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg       = SpecBase.messages(application)
          val result             = route(application, onPageLoadRequest).value
          val netAmount          = incomeSummary.getNetBusinessProfitOrLossForTaxPurposes()
          val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
          val tables             = buildTables(netBusinessProfitOrLossValues, Loss)

          val expectedView: String = {
            val view = application.injector.instanceOf[CheckNetProfitLossView]
            view(userType, Loss, formattedNetAmount, tables, onwardLossRoute)(onPageLoadRequest, msg)
              .toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
        s"when net profit and user is an $userType" in {
          val incomeSummary                 = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetProfit
          val netBusinessProfitOrLossValues = NetBusinessProfitOrLossValuesBuilder.aNetBusinessProfitValues
          val stubService = SelfEmploymentServiceStub(
            getBusinessIncomeSourcesSummaryResult = Right(incomeSummary),
            getNetBusinessProfitOrLossValuesResult = Right(netBusinessProfitOrLossValues)
          )
          val application        = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg       = SpecBase.messages(application)
          val result             = route(application, onPageLoadRequest).value
          val netAmount          = incomeSummary.getNetBusinessProfitOrLossForTaxPurposes()
          val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
          val tables             = buildTables(netBusinessProfitOrLossValues, Profit)

          val expectedView: String = {
            val view = application.injector.instanceOf[CheckNetProfitLossView]
            view(userType, Profit, formattedNetAmount, tables, onwardProfitRoute)(onPageLoadRequest, msg)
              .toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }
    }
  }

}
