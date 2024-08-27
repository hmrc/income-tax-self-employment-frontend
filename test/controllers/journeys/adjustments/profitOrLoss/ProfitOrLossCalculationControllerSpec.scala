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
import controllers.journeys
import models.NormalMode
import models.database.UserAnswers
import models.common.Journey.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import utils.Assertions.assertEqualWithDiff
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary._
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView

class ProfitOrLossCalculationControllerSpec extends ControllerSpec {

  def userAnswers: UserAnswers = buildUserAnswers(Json.obj())

  def onPageLoadRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoad)

  def onPageLoad: String = routes.ProfitOrLossCalculationController.onPageLoad(taxYear, businessId).url

  def onwardRoute: Call = journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss, NormalMode)

  "onPageLoad" - {
    "should return OK and render correct view" - {
      userTypeCases.foreach { userType =>
        s"when net profit and user is an $userType" in {
          val incomeSummary          = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetProfit
          val stubService            = SelfEmploymentServiceStub(getBusinessIncomeSourcesSummaryResult = Right(incomeSummary))
          val application            = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg: Messages = SpecBase.messages(application)
          val result                 = route(application, onPageLoadRequest).value
          val netAmount              = BigDecimal(100.00)
          val formattedNetAmount     = formatSumMoneyNoNegative(List(netAmount))
          val profitOrLoss           = Profit
          val tables = AdjustedTaxableProfitOrLossSummary(
            buildYourAdjustedProfitOrLossTable(taxYear, profitOrLoss),
            buildNetProfitOrLossTable(profitOrLoss),
            buildExpensesTable(profitOrLoss),
            buildCapitalAllowancesTable(profitOrLoss),
            buildAdjustmentsTable()
          )
          val expectedView = {
            val view = application.injector.instanceOf[ProfitOrLossCalculationView]
            view(
              userType,
              formattedNetAmount,
              taxYear,
              profitOrLoss,
              tables,
              onwardRoute
            )(onPageLoadRequest, msg).toString()
          }
          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }

        s"when net loss and user is an $userType" in {
          val incomeSummary          = BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummaryWithNetLoss
          val stubService            = SelfEmploymentServiceStub(getBusinessIncomeSourcesSummaryResult = Right(incomeSummary))
          val application            = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
          implicit val msg: Messages = SpecBase.messages(application)
          val result                 = route(application, onPageLoadRequest).value
          val netAmount              = BigDecimal(100.00)
          val formattedNetAmount     = formatSumMoneyNoNegative(List(netAmount))
          val profitOrLoss           = Loss
          val tables = AdjustedTaxableProfitOrLossSummary(
            buildYourAdjustedProfitOrLossTable(taxYear, profitOrLoss),
            buildNetProfitOrLossTable(profitOrLoss),
            buildExpensesTable(profitOrLoss),
            buildCapitalAllowancesTable(profitOrLoss),
            buildAdjustmentsTable()
          )
          val expectedView = {
            val view = application.injector.instanceOf[ProfitOrLossCalculationView]
            view(
              userType,
              formattedNetAmount,
              taxYear,
              profitOrLoss,
              tables,
              onwardRoute
            )(onPageLoadRequest, msg).toString()
          }
          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }
    }
  }
}
