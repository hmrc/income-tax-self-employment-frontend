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

package controllers.journeys.abroad

import base.SpecBase
import builders.UserBuilder
import models.NormalMode
import models.common.BusinessId
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.Journey.Abroad
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.abroad.SelfEmploymentAbroadSummary
import viewmodels.govuk.SummaryListFluency
import views.html.journeys.abroad.SelfEmploymentAbroadCYAView

import scala.concurrent.Future

class SelfEmploymentAbroadCYAControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  private val businessID = BusinessId("trade-details" + "-" + UserBuilder.aNoddyUser.nino)

  private lazy val requestUrl = controllers.journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessID).url

  private lazy val nextRoute =
    controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessID, Abroad.toString, NormalMode).url

  "SelfEmploymentAbroadCYAController" - {
    "when user answers are present" - {
      "must return OK and the correct view for a GET" in {
        val userAnswers                 = UserAnswers("someId", Json.obj("trade-details-nino" -> Json.obj("selfEmploymentAbroad" -> true)))
        val application                 = applicationBuilder(userAnswers = Some(userAnswers)).build()
        val selfEmploymentAbroadCYAView = application.injector.instanceOf[SelfEmploymentAbroadCYAView]

        running(application) {
          val expectedMaybeSummaryListRow = SelfEmploymentAbroadSummary.row(taxYear, Individual, businessID, userAnswers)(messages(application))
          val expectedSummaryList         = SummaryList(Seq(expectedMaybeSummaryListRow))

          val request                = FakeRequest(GET, requestUrl)
          val result: Future[Result] = route(application, request).value

          status(result) shouldBe OK

          contentAsString(result) shouldBe selfEmploymentAbroadCYAView(taxYear, expectedSummaryList, nextRoute, Individual)(
            request,
            messages(application)).toString
        }
      }
    }
    "when user answers are not provided" - {
      "must redirect to the JourneyRecoveryController for a GET" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request                = FakeRequest(GET, requestUrl)
          val result: Future[Result] = route(application, request).value

          status(result) shouldBe SEE_OTHER
        }
      }
    }
  }

}
