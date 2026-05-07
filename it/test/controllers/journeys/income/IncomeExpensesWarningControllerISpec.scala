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
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.common.Journey.Income
import models.common.JourneyAnswersContext
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.test.Helpers._

class IncomeExpensesWarningControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String       = routes.IncomeExpensesWarningController.onPageLoad(taxYear, businessId).url
  val submitUrl: String = routes.IncomeExpensesWarningController.onSubmit(taxYear, businessId).url

  private val incomeContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, Income)

  "GET /:taxYear/:businessId/income/expenses-warning" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        DbHelper.insertEmpty()

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

  "POST /:taxYear/:businessId/income/expenses-warning" when {
    "the user has a turnover amount in session and submits" must {
      "redirect to the income CYA page" in {
        AuthStub.authorised()
        DbHelper.insertWithData(Json.obj(businessId.value -> Json.obj("turnoverIncomeAmount" -> BigDecimal(1000))))
        AnswersApiStub.getAnswers(incomeContext)(NOT_FOUND)

        val result = await(buildClient(submitUrl).post(Map.empty[String, Seq[String]]))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("check-your-income")
      }
    }
  }
}
