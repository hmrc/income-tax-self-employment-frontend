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

package views.journeys.industrySectors

import forms.industrysectors.FarmerOrMarketGardenerFormProvider
import models.NormalMode
import models.common.UserType
import models.common.UserType.Individual
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.ViewBaseSpec
import views.html.journeys.industrysectors.FarmerOrMarketGardenerView

class FarmerOrMarketGardenerViewSpec extends ViewBaseSpec {

  private val view   = app.injector.instanceOf[FarmerOrMarketGardenerView]
  val formProvider   = new FarmerOrMarketGardenerFormProvider()
  val individualForm = formProvider(UserType.Individual)
  val agentForm      = formProvider(UserType.Agent)

  object ExpectedIndividual {
    val title    = "Were you a farmer or market gardener?"
    val heading  = "Were you a farmer or market gardener?"
    val continue = "Continue"
    val error    = "Select yes if you worked as a farmer or market gardener"
  }

  object ExpectedAgent {
    val title   = "Was your client a farmer or market gardener?"
    val heading = "Was your client a farmer or market gardener?"
    val error   = "Select yes if your client worked as a farmer or market gardener"
  }

  def individualView(userType: UserType): Document = Jsoup.parse(
    view(individualForm, userType, taxYear, businessId, NormalMode)(fakeRequest, messages).body
  )

  def agentView(userType: UserType): Document = Jsoup.parse(
    view(agentForm, userType, taxYear, businessId, NormalMode)(fakeRequest, messages).body
  )

  def errorView(form: Form[Boolean], userType: UserType): Document = Jsoup.parse(
    view(form.bind(Map("value" -> "")), userType, taxYear, businessId, NormalMode)(fakeRequest, messages).body
  )

  "The FarmerOrMarketGardenerViewSpec" when {
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
        button.attr("href") mustBe "" // TODO: Replace with nextRoute once navigation is implemented
      }

      "display the correct error message when no option is selected" in {
        val errorDoc = errorView(individualForm, Individual)
        errorDoc.select(".govuk-error-message").text() must include(ExpectedIndividual.error)
        errorDoc.select(".govuk-error-summary__list li").text() mustBe ExpectedIndividual.error
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

      "display the correct error message when no option is selected" in {
        val errorDoc = errorView(agentForm, UserType.Agent)
        errorDoc.select(".govuk-error-message").text() must include(ExpectedAgent.error)
        errorDoc.select(".govuk-error-summary__list li").text() mustBe ExpectedAgent.error
      }
    }
  }
}
