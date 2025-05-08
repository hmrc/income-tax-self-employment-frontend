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

package controllers.journeys.industrySectors

import base.IntegrationBaseSpec
import controllers.journeys.abroad.routes
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.IndustrySectors
import models.common.JourneyAnswersContext
import models.journeys.industrySectors.IndustrySectorsDb
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.NOT_FOUND

class SelfEmploymentAbroadControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String                  = routes.SelfEmploymentAbroadController.onSubmit(taxYear, businessId, NormalMode).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, IndustrySectors)

  val testIndustrySectors: IndustrySectorsDb = IndustrySectorsDb(
    isFarmerOrMarketGardener = Some(true),
    hasProfitFromCreativeWorks = Some(true),
    isAllSelfEmploymentAbroad = Some(true)
  )

  "GET /:taxYear/:businessId/industry-sectors/self-employment-abroad" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value][checked]").isEmpty mustBe false
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value][checked]").isEmpty mustBe false
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

  "POST /:taxYear/:businessId/industry-sectors/self-employment-abroad" when {
    "the user selects 'Yes'" must {
      "redirect to the next page" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors.copy(isAllSelfEmploymentAbroad = None))))
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testIndustrySectors))(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("true"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }

    "the user selects 'No'" must {
      "redirect to the next page" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors.copy(isAllSelfEmploymentAbroad = None))))
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testIndustrySectors.copy(isAllSelfEmploymentAbroad = Some(false))))(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("false"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe defined
      }
    }

    "the user submits without selecting an option" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
