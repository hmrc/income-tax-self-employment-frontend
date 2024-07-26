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
import common.TestApp.buildAppFromUserType
import models.NormalMode
import models.database.UserAnswers
import models.journeys.adjustments.ProfitOrLoss
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import utils.Assertions.assertEqualWithDiff
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{buildTable1, buildTable2, buildTable3}
import views.html.journeys.adjustments.profitOrLoss.CheckNetProfitLossView

class CheckNetProfitLossControllerSpec extends ControllerSpec {

  def userAnswers: UserAnswers = buildUserAnswers(Json.obj())

  def onPageLoad: String = routes.CheckNetProfitLossController.onPageLoad(taxYear, businessId).url
  def onwardRoute: Call  = routes.CurrentYearLossesController.onPageLoad(taxYear, businessId, NormalMode)

  def onPageLoadRequest = FakeRequest(GET, onPageLoad)

  // TODO SASS-8626 replace with 'ProfitOrLoss.values' to test profit and loss scenarios when controller is dynamic
  val profitOrLossCases = List(ProfitOrLoss.Loss)
  val defaultNetAmount  = BigDecimal(-200) // TODO SASS-8626 remove and add tests for different API values

  "onPageLoad" - {
    "should return Ok and render correct view" - {
      profitOrLossCases.foreach { profitOrLoss =>
        userTypeCases.foreach { userType =>
          s"when net $profitOrLoss and user is an $userType" in {
            val application  = buildAppFromUserType(userType, Some(userAnswers))
            implicit val msg = SpecBase.messages(application)
            val result       = route(application, onPageLoadRequest).value
            val netAmount    = formatSumMoneyNoNegative(List(defaultNetAmount))
            val table1       = buildTable1(profitOrLoss, 3000, 0.05, -3100)
            val table2       = buildTable2(profitOrLoss, 0, -0.05, 100.20)
            val table3       = buildTable3(profitOrLoss, 200, -200.1)

            val expectedView: String = {
              val view = application.injector.instanceOf[CheckNetProfitLossView]
              view(userType, profitOrLoss, netAmount, table1, table2, table3, onwardRoute)(onPageLoadRequest, msg)
                .toString()
            }

            status(result) mustBe OK
            assertEqualWithDiff(contentAsString(result), expectedView)
          }
        }
      }
    }
  }

}
