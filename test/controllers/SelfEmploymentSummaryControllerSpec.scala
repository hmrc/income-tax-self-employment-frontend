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

package controllers

import base.SpecBase
import connectors.SelfEmploymentConnector
import connectors.builders.BusinessDataBuilder.{aGetBusinessNoneResponse, aGetBusinessResponse}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SelfEmploymentSummaryView
import viewmodels.govuk.SummaryListFluency
import viewmodels.summary.SelfEmploymentSummaryViewModel.row
import play.api.inject.bind

import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentSummaryControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val tradingNames: Seq[Option[String]] = Seq(Some("Trade one"), Some("Trade two"))
  val emptyTradingNames: Seq[Option[String]] = Seq()
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val userAnswers = UserAnswers("1345566")

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "SelfEmploymentSummary Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view when there are no self-employments" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[SelfEmploymentConnector].toInstance(mockConnector)).build()

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(Right(Seq()))

          val request = FakeRequest(GET, routes.SelfEmploymentSummaryController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val emptySummaryList = SummaryList(rows =
            emptyTradingNames.map(name => row(s"${name.getOrElse("")}")(messages(application)))
          )

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(emptySummaryList)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there are None trading names" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[SelfEmploymentConnector].toInstance(mockConnector)).build()

        val noneTradingNames: Seq[Option[String]] = Seq(None, None)

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(aGetBusinessNoneResponse)

          val request = FakeRequest(GET, routes.SelfEmploymentSummaryController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]

          val noneSummaryList = SummaryList(rows =
            noneTradingNames.map(name => row(s"${name.getOrElse("")}")(messages(application)))
          )

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noneSummaryList)(request, messages(application)).toString
        }
      }


      "must return OK and the correct view for a GET when self-employment data exist" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[SelfEmploymentConnector].toInstance(mockConnector)).build()

        running(application) {

          when(mockConnector.getBusinesses(any, any)(any, any)) thenReturn Future(aGetBusinessResponse)

          val request = FakeRequest(GET, routes.SelfEmploymentSummaryController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentSummaryView]
          val summaryList = SummaryList(rows = tradingNames.map(name => row(s"${name.getOrElse("")}")(messages(application))))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(summaryList)(request, messages(application)).toString
        }
      }

    }
  }
}
