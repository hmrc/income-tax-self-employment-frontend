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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers._

class TradingAllowanceControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String       = routes.TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String = routes.TradingAllowanceController.onSubmit(taxYear, businessId, NormalMode).url

  "GET /:taxYear/:businessId/income/trading-allowance" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        DbHelper.insertWithAccountingType()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
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

  "POST /:taxYear/:businessId/income/trading-allowance" when {
    "the user submits without selecting an option" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        DbHelper.insertWithAccountingType()

        val result = await(buildClient(submitUrl).post(Map[String, Seq[String]]("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
