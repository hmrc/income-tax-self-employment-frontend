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

package controllers.journeys.industrySectors

import base.IntegrationBaseSpec
import controllers.journeys.industrysectors.routes
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.common.Journey.IndustrySectors
import models.common.JourneyAnswersContext
import models.journeys.industrySectors.IndustrySectorsDb
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers._

class IndustrySectorsAndAbroadCYAControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.IndustrySectorsAndAbroadCYAController.onPageLoad(taxYear, businessId).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, IndustrySectors)

  val testIndustrySectors: IndustrySectorsDb = IndustrySectorsDb(
    isFarmerOrMarketGardener = Some(true),
    hasProfitFromCreativeWorks = Some(true)
  )

  "IndustrySectorsAndAbroadCYAControllerISpec" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
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
}
