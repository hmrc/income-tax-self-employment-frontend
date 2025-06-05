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

package views.journeys.expenses

import forms.travelAndAccommodation.TravelAndAccommodationTotalExpensesFormProvider
import models.NormalMode
import models.common.UserType
import models.common.UserType.Individual
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationTotalExpensesView

class TravelAndAccommodationTotalExpensesViewSpec extends ViewBaseSpec {

  private val view      = app.injector.instanceOf[TravelAndAccommodationTotalExpensesView]
  val formProvider      = new TravelAndAccommodationTotalExpensesFormProvider()
  val individualForm    = formProvider(UserType.Individual)
  val agentForm         = formProvider(UserType.Agent)
  private val nextRoute = ""

  object ExpectedIndividual {
    val title        = "How much did you spend on travel and accommodation for work?"
    val heading      = "How much did you spend on travel and accommodation for work?"
    val continue     = "Continue"
    val noInputError = "Enter your total travel and accommodation expenses"
  }

  object ExpectedAgent {
    val title   = "How much did your client spend on travel and accommodation for work?"
    val heading = "How much did your client spend on travel and accommodation for work?"
  }

  def individualView(userType: UserType): Document = Jsoup.parse(
    view(individualForm, NormalMode, userType, taxYear, businessId)(fakeRequest, messages).body
  )

  def agentView(userType: UserType): Document = Jsoup.parse(
    view(agentForm, NormalMode, userType, taxYear, businessId)(fakeRequest, messages).body
  )

  def errorView(form: Form[BigDecimal], userType: UserType): Document = Jsoup.parse(
    view(form.bind(Map("value" -> "")), NormalMode, userType, taxYear, businessId)(fakeRequest, messages).body
  )

  "The CheckYourSelfEmploymentDetailsView" when {
    "the user is an Individual" must {
      val individualPage = individualView(Individual)

      "have the correct title" in {
        individualPage.title must include(ExpectedIndividual.title)
      }

      "have the correct heading" in {
        individualPage.select("h1").text() mustBe ExpectedIndividual.heading
      }

      "have a continue button with the correct text and link" in {
        val button = individualPage.select(".govuk-button")
        button.text() mustBe ExpectedIndividual.continue
        button.attr("href") mustBe nextRoute
      }

      "display the correct error message when no input is entered" in {
        val errorDoc = errorView(individualForm, Individual)
        errorDoc.select(".govuk-error-message").text() must include(ExpectedIndividual.noInputError)
        errorDoc.select(".govuk-error-summary__list li").text() mustBe ExpectedIndividual.noInputError
      }
    }

    "the user is an Agent" must {
      val agentPage = agentView(UserType.Agent)

      "have the correct title" in {
        agentPage.title must include(ExpectedAgent.title)
      }

      "have the correct heading" in {
        agentPage.select("h1").text() mustBe ExpectedAgent.heading
      }
    }
  }
}
