/*
 * Copyright 2025 HM Revenue & Customs
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

import base.IntegrationBaseSpec
import helpers.{AuthStub, WiremockSpec}
import models.NormalMode
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers._

class TradingAllowanceAmountControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String       = routes.TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String = routes.TradingAllowanceAmountController.onSubmit(taxYear, businessId, NormalMode).url

  private def seedWithTurnoverAmount(): Unit =
    DbHelper.insertWithData(Json.obj(businessId.value -> Json.obj("turnoverIncomeAmount" -> BigDecimal(1000))))

  "GET /:taxYear/:businessId/income/trading-allowance/amount" when {
    "the user is an agent with a turnover amount in session" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        seedWithTurnoverAmount()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual with a turnover amount in session" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        seedWithTurnoverAmount()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION).exists(_.contains("gg-sign-in")) mustBe true
        result.status mustBe SEE_OTHER
      }
    }
  }

  "POST /:taxYear/:businessId/income/trading-allowance/amount" when {
    "the user submits an invalid (non-numeric) amount" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        seedWithTurnoverAmount()

        val result = await(buildClient(submitUrl).post(Map[String, Seq[String]]("value" -> Seq("not-a-number"))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
