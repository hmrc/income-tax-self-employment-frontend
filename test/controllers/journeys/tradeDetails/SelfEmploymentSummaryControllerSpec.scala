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

package controllers.journeys.tradeDetails

import base.SpecBase
import builders.BusinessDataBuilder.aBusinessDataNoneTradeNames
import controllers.journeys
import controllers.journeys.tradeDetails
import controllers.journeys.tradeDetails.SelfEmploymentSummaryController.generateRowList
import models.NormalMode
import models.common.BusinessId
import models.database.UserAnswers
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.tradeDetails.SelfEmploymentSummaryViewModel.row
import viewmodels.govuk.SummaryListFluency
import views.html.journeys.tradeDetails.SelfEmploymentSummaryView

class SelfEmploymentSummaryControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val stubService = SelfEmploymentServiceStub()
  val userAnswers = UserAnswers("1345566")
  val businessID  = BusinessId("trade-details")

  "SelfEmploymentSummary Controller" - {

    "onPageLoad" - {

      def nextRoute = journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessID, "trade-details", NormalMode).url

      "must return OK and the correct view when there are no self-employments" in {

        val application = createApp(stubService.copy(getBusinessesResult = Right(Seq())))

        running(application) {

          val request = FakeRequest(GET, tradeDetails.routes.SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val emptyTradingNames: Seq[Option[String]] = Seq()
          val emptySummaryList = SummaryList(rows = emptyTradingNames.map(name => row(s"${name.getOrElse("")}")(messages(application))))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(emptySummaryList, nextRoute)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there are None trading names" in {

        val application = createApp(stubService.copy(getBusinessesResult = Right(Seq(aBusinessDataNoneTradeNames))))

        running(application) {

          val request = FakeRequest(GET, tradeDetails.routes.SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val noneTradingNames = Seq(("", BusinessId("businessId-0-1")))
          val noneSummaryList  = generateRowList(taxYear, noneTradingNames)(messages(application))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noneSummaryList, nextRoute)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when self-employment data exist" in {

        val application = createApp(stubService)

        running(application) {

          val request = FakeRequest(GET, tradeDetails.routes.SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val tradingNames = Seq(("Trade one", BusinessId("businessId-1")))
          val summaryList  = generateRowList(taxYear, tradingNames)(messages(application))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(summaryList, nextRoute)(request, messages(application)).toString
        }
      }

    }
  }

}
