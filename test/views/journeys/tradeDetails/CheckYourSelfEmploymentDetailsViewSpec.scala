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

package views.journeys.tradeDetails

import models.common.UserType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.ViewBaseSpec
import views.html.journeys.tradeDetails.CheckYourSelfEmploymentDetailsView

class CheckYourSelfEmploymentDetailsViewSpec extends ViewBaseSpec {

  private val view      = app.injector.instanceOf[CheckYourSelfEmploymentDetailsView]
  private val nextRoute = "/foo"

  object ExpectedIndividual {
    val title    = "Check your self-employment details"
    val heading  = "Check your self-employment details"
    val continue = "Continue"

    val row1Key   = "What name did you use for your self-employment"
    val row1Value = "Trade one"
    val row2Key   = "What did you do for your self-employment"
    val row2Value = "self-employment"
    val row3Key   = "When did your self-employment start"
    val row3Value = "6 April 2023"
  }

  object ExpectedAgent {
    val title   = "Check your client’s self-employment details"
    val heading = "Check your client’s self-employment details"

    val row1Key = "What name did your client use for their self-employment"
    val row2Key = "What did your client do for their self-employment"
    val row3Key = "When did your client’s self-employment start"
  }

  private def summaryList(userType: UserType): SummaryList = SummaryList(rows = Seq(
    SummaryListRow(
      key = Key(Text(if (userType == UserType.Individual) ExpectedIndividual.row1Key else ExpectedAgent.row1Key)),
      value = Value(Text(ExpectedIndividual.row1Value))
    ),
    SummaryListRow(
      key = Key(Text(if (userType == UserType.Individual) ExpectedIndividual.row2Key else ExpectedAgent.row2Key)),
      value = Value(Text(ExpectedIndividual.row2Value))
    ),
    SummaryListRow(
      key = Key(Text(if (userType == UserType.Individual) ExpectedIndividual.row3Key else ExpectedAgent.row3Key)),
      value = Value(Text(ExpectedIndividual.row3Value))
    )
  ))

  def view(userType: UserType): Document = Jsoup.parse(
    view(summaryList(userType), taxYear, userType, nextRoute)(fakeRequest, messages).body
  )

  "The CheckYourSelfEmploymentDetailsView" when {
    "the user is an Individual" must {
      val individualPage = view(UserType.Individual)

      "have the correct title" in {
        individualPage.title must include(ExpectedIndividual.title)
      }

      "have the correct heading" in {
        individualPage.select("h1").text() mustBe ExpectedIndividual.heading
      }

      "display all summary list rows" in {

        individualPage.summaryRow(1) mustBe Some(
          SummaryRow(ExpectedIndividual.row1Key, ExpectedIndividual.row1Value, Seq.empty)
        )
        individualPage.summaryRow(2) mustBe Some(
          SummaryRow(ExpectedIndividual.row2Key, ExpectedIndividual.row2Value, Seq.empty)
        )
        individualPage.summaryRow(3) mustBe Some(
          SummaryRow(ExpectedIndividual.row3Key, ExpectedIndividual.row3Value, Seq.empty)
        )
      }

      "have a continue button with the correct text and link" in {
        val button = individualPage.select(".govuk-button")
        button.text() mustBe ExpectedIndividual.continue
        button.attr("href") mustBe nextRoute
      }
    }

    "the user is an Agent" must {
      val agentPage = view(UserType.Agent)

      "have the correct title" in {
        agentPage.title must include(ExpectedAgent.title)
      }

      "have the correct heading" in {
        agentPage.select("h1").text() mustBe ExpectedAgent.heading
      }

      "display summary list rows with agent-specific keys" in {
        agentPage.summaryRow(1) mustBe Some(
          SummaryRow(ExpectedAgent.row1Key, ExpectedIndividual.row1Value, Seq.empty)
        )
        agentPage.summaryRow(2) mustBe Some(
          SummaryRow(ExpectedAgent.row2Key, ExpectedIndividual.row2Value, Seq.empty)
        )
        agentPage.summaryRow(3) mustBe Some(
          SummaryRow(ExpectedAgent.row3Key, ExpectedIndividual.row3Value, Seq.empty)
        )
      }
    }
  }
}
