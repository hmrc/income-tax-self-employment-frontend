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
import builders.BusinessDataBuilder.{aBusinessDataNoneResponse, aBusinessDataResponse}
import builders.UserBuilder
import connectors.SelfEmploymentConnector
import controllers.journeys.routes.SectionCompletedStateController
import controllers.journeys.tradeDetails.SelfEmploymentSummaryController.generateRowList
import controllers.journeys.tradeDetails.routes.SelfEmploymentSummaryController
import models.NormalMode
import models.common.BusinessId
import models.database.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.tradeDetails.SelfEmploymentSummaryViewModel.row
import viewmodels.govuk.SummaryListFluency
import views.html.journeys.tradeDetails.SelfEmploymentSummaryView

import scala.concurrent.Future

class SelfEmploymentSummaryControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val userAnswers                            = UserAnswers("1345566")
  val businessID                             = BusinessId("trade-details" + "-" + UserBuilder.aNoddyUser.nino)

  "SelfEmploymentSummary Controller" - {

    "onPageLoad" - {

      def nextRoute = SectionCompletedStateController.onPageLoad(taxYear, businessID, "trade-details", NormalMode).url

      "must return OK and the correct view when there are no self-employments" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(Right(Seq()))

          val request = FakeRequest(GET, SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val emptyTradingNames: Seq[Option[String]] = Seq()
          val emptySummaryList = SummaryList(rows = emptyTradingNames.map(name => row(s"${name.getOrElse("")}")(messages(application))))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(emptySummaryList, nextRoute)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there are None trading names" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(aBusinessDataNoneResponse)

          val request = FakeRequest(GET, SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val noneTradingNames = Seq(("", BusinessId("businessId-0-1")), ("", BusinessId("businessId-0-2")))
          val noneSummaryList  = generateRowList(taxYear, noneTradingNames)(messages(application))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noneSummaryList, nextRoute)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when self-employment data exist" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(aBusinessDataResponse)

          val request = FakeRequest(GET, SelfEmploymentSummaryController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val tradingNames = Seq(("Trade one", BusinessId("businessId-1")), ("Trade two", BusinessId("businessId-2")))
          val summaryList  = generateRowList(taxYear, tradingNames)(messages(application))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(summaryList, nextRoute)(request, messages(application)).toString
        }
      }

    }
  }

}
