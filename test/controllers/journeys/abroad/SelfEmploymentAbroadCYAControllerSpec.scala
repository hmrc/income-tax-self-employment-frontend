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
import models.{Abroad, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.SelfEmploymentAbroadPage
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers.SelfEmploymentAbroadSummary
import viewmodels.govuk.SummaryListFluency
import views.html.SelfEmploymentAbroadCYAView

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class SelfEmploymentAbroadCYAControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  val agent = false
  val taxYear: Int = LocalDate.now().getYear


  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "SelfEmploymentAbroadCYA Controller" - {

    "onPageLoad" - {

      val nextRoute = controllers.journeys.routes.SectionCompletedStateController.onPageLoad(
        taxYear, Abroad.toString, NormalMode).url

      "must return OK and the correct view for a GET" in {

        val userAnswers = UserAnswers(userAnswersId).set(SelfEmploymentAbroadPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SelfEmploymentAbroadCYAView]

          val abroadSummary = SummaryList(Seq(SelfEmploymentAbroadSummary.row(taxYear, agent, userAnswers)(messages(application)).get))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(taxYear, abroadSummary, nextRoute, agent)(request, messages(application)).toString
        }
      }
    }
  }
}
