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

package controllers.journeys.expenses.travelAndAccommodation

import base.IntegrationBaseSpec
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.ExpensesTravelForWork
import models.common.JourneyAnswersContext
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers._

class TravelAndAccommodationDisallowableExpensesControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.TravelAndAccommodationDisallowableExpensesController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String                  = routes.TravelAndAccommodationDisallowableExpensesController.onSubmit(taxYear, businessId, NormalMode).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesTravelForWork)

  "GET /:taxYear/:businessId/expenses/travel/travel-accommodation-disallowable-expenses " when {
    "the user is an agent" must {
      "return OK with the correct view" in {

        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData.copy(disallowableTravelExpenses = None))))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "redirect to 'technical difficulties' page when missing total Expenses data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData.copy(totalTravelExpenses = None))))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.standard.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())
        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value]").`val`() mustBe "400"
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData.copy(disallowablePublicTransportExpenses = None))))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value]").isEmpty mustBe false
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

  "POST /:taxYear/:businessId/expenses/travel/travel-accommodation-disallowable-expenses" when {
    "the user enters valid travel and accommodation expenses" must {
      "redirect to the next page" in {

        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData.copy(disallowableTravelExpenses = None))))
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testTravelAndAccommodationData))(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("400.00"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe
          Some(
            controllers.journeys.expenses.travelAndAccommodation.routes.TravelAndAccommodationDisallowableExpensesCYAController
              .onPageLoad(taxYear, businessId)
              .url
          )
      }
    }

    "redirect to 'technical difficulties' page when missing total Expenses data" in {
      AuthStub.authorised()
      AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData.copy(totalTravelExpenses = None))))
      AnswersApiStub.replaceAnswers(testContext, Json.toJson(testTravelAndAccommodationData))(OK)
      DbHelper.insertEmpty()

      val result = await(buildClient(submitUrl).post(Map("value" -> Seq("400.00"))))

      result.status mustBe SEE_OTHER
      result.header(HeaderNames.LOCATION) mustBe Some(controllers.standard.routes.JourneyRecoveryController.onPageLoad().url)
    }

    "the user submits without entering a vehicle expenses" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelAndAccommodationData)))
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
