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
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers._

class IncomeCYAControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String       = routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
  val submitUrl: String = routes.IncomeCYAController.onSubmit(taxYear, businessId).url

  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, Income)

  "GET /:taxYear/:businessId/income/check-your-income" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertWithAccountingType()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertWithAccountingType()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertWithAccountingType()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION).exists(_.contains("gg-sign-in")) mustBe true
        result.status mustBe SEE_OTHER
      }
    }
  }

  "POST /:taxYear/:businessId/income/check-your-income" when {
    "the user submits with income session data" must {
      "submit the answers and redirect to the section completed page" in {
        AuthStub.authorised()
        DbHelper.insertWithData(Json.obj(businessId.value -> Json.obj(
          "accountingType"             -> "ACCRUAL",
          "incomeNotCountedAsTurnover" -> false,
          "turnoverIncomeAmount"       -> 1000,
          "anyOtherIncome"             -> false,
          "tradingAllowance"           -> "useTradingAllowance"
        )))
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        stubPutWithResponseBody(
          s"/income-tax-self-employment/answers/users/${nino.value}/businesses/${businessId.value}/years/${taxYear.endYear}/journeys/income",
          OK,
          "{}")

        val result = await(buildClient(submitUrl).post(Map.empty[String, Seq[String]]))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }
  }
}
