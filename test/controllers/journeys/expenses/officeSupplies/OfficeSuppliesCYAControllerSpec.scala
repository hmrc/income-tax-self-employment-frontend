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

package controllers.journeys.expenses.officeSupplies

import base.SpecBase
import controllers.journeys.expenses.officeSupplies.routes.OfficeSuppliesCYAController
import models.NormalMode
import models.database.UserAnswers
import models.journeys.Journey.ExpensesOfficeSupplies
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.officeSupplies.{OfficeSuppliesAmountSummary, OfficeSuppliesDisallowableAmountSummary}
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

import scala.concurrent.Future

class OfficeSuppliesCYAControllerSpec extends SpecBase {

  private val authUserTypes = List("individual", "agent")

  private val userAnswerData = Json.parse(s"""
       |{
       |  "$businessId": {
       |    "officeSuppliesAmount": 200.00,
       |    "officeSuppliesDisallowableAmount": 100.00
       |  }
       |}
       |""".stripMargin)

  private val userAnswers = UserAnswers(userAnswersId, userAnswerData.as[JsObject])

  "OfficeSuppliesCYAController" - {
    authUserTypes.foreach { authUser =>
      s"when handling a request from a $authUser to load a page" - {
        "must return a 200 OK with the view" in {
          val application = applicationBuilder(Some(userAnswers), isAgent(authUser)).build()

          implicit val appMessages: Messages = messages(application)

          running(application) {
            val view = application.injector.instanceOf[OfficeSuppliesCYAView]

            implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, OfficeSuppliesCYAController.onPageLoad(taxYear, businessId).url)

            val expectedSummaryListRows = Seq(
              OfficeSuppliesAmountSummary.row(userAnswers, taxYear, businessId, authUser),
              OfficeSuppliesDisallowableAmountSummary.row(userAnswers, taxYear, businessId, authUser)
            ).flatten

            val expectedSummaryLists = SummaryList(rows = expectedSummaryListRows, classes = "govuk-!-margin-bottom-7")

            val result: Future[Result] = route(application, request).value

            status(result) shouldBe OK

            val expectedNextRoute =
              controllers.journeys.routes.SectionCompletedStateController
                .onPageLoad(taxYear, businessId, ExpensesOfficeSupplies.toString, NormalMode)
                .url

            contentAsString(result) mustEqual view(authUser, expectedSummaryLists, taxYear, expectedNextRoute).toString
          }
        }
      }

    }

  }

}
