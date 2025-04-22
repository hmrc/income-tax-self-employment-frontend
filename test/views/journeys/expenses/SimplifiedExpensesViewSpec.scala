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

import forms.expenses.travelAndAccommodation.SimplifiedExpenseFormProvider
import models.common.UserType
import models.{Index, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.SimplifiedExpensesView

class SimplifiedExpensesViewSpec extends ViewBaseSpec {

  val application: Application     = new GuiceApplicationBuilder().build()
  val view: SimplifiedExpensesView = application.injector.instanceOf[SimplifiedExpensesView]

  private val formProvider    = app.injector.instanceOf[SimplifiedExpenseFormProvider]
  private val index: Index    = Index(1)
  private val vehicle: String = "Car"

  def form(userType: UserType): Form[Boolean] = formProvider(userType, vehicle)

  def view(userType: UserType, mode: Mode, form: Form[Boolean]): Document =
    Jsoup.parse(
      view(form, userType, taxYear, businessId, index, mode, vehicle)(fakeRequest, messages).body
    )

  object Expected {
    val heading = "Simplified expenses for vehicles"
    val p1 = "Simplified expenses are a way of calculating some of your expenses on a pence-per-mile basis instead of working out your actual costs."
    val p2 = "If you’ve used Car in previous tax years, you may have already used simplified expenses."
    val subHeading = "Have you used simplified expenses for Car before?"
    val error      = "Select yes if you’ve used simplified expenses for Car before"
    val button     = "Continue"
  }

  object ExpectedAgent {
    val heading = "Simplified expenses for vehicles"
    val p1 =
      "Simplified expenses are a way of calculating some of your client’s expenses on a pence-per-mile basis instead of working out your actual costs."
    val p2         = "If your client has used Car in previous tax years, they may have already used simplified expenses."
    val subHeading = "Has your client used simplified expenses for Car before?"
    val error      = "Select yes if your client has used simplified expenses for Car before"
  }

  "The SimplifiedExpensesViewSpec" when {
    "the user is an Individual" must {
      val individualPage = view(UserType.Individual, NormalMode, form(UserType.Individual))

      "have the correct title" in {
        individualPage.title must include(Expected.heading)
      }

      "have the correct heading" in {
        individualPage.heading mustBe Some(Expected.heading)
      }

      "have the correct message for paragraph 1" in {
        individualPage.para(1) mustBe Some(Expected.p1)
      }

      "have the correct message for paragraph 2" in {
        individualPage.para(2) mustBe Some(Expected.p2)
      }

      "display correct boolean label" in {
        individualPage.radio("true") mustBe Some("Yes")
        individualPage.radio("false") mustBe Some("No")
      }

      "have a correct sub-heading" in {
        individualPage.legend(1) mustBe Some(Expected.subHeading)
      }

      "show the mandatory field error and error summary when the form is blank" in {
        val formWithErrors = form(UserType.Individual).bind(Map("value" -> ""))
        val individualPage = view(UserType.Individual, NormalMode, formWithErrors)

        individualPage.errorSummaryLinks mustBe List(
          Link(href = "#value", text = Expected.error)
        )
      }

      "have a submit button" in {
        individualPage.submitButton mustBe Some(Expected.button)
      }
    }

    "the user is an Agent" must {
      val agentPage = view(UserType.Agent, NormalMode, form(UserType.Agent))

      "have the correct title" in {
        agentPage.title must include(ExpectedAgent.heading)
      }

      "have the correct heading" in {
        agentPage.heading mustBe Some(ExpectedAgent.heading)
      }

      "have the correct message for paragraph 1" in {
        agentPage.para(1) mustBe Some(ExpectedAgent.p1)
      }

      "have the correct message for paragraph 2" in {
        agentPage.para(2) mustBe Some(ExpectedAgent.p2)
      }

      "have a correct sub-heading" in {
        agentPage.legend(1) mustBe Some(ExpectedAgent.subHeading)
      }

      "show the mandatory field error and error summary when the form is blank" in {
        val formWithErrors = form(UserType.Agent).bind(Map("value" -> ""))
        val individualPage = view(UserType.Agent, NormalMode, formWithErrors)

        individualPage.errorSummaryLinks mustBe List(
          Link(href = "#value", text = ExpectedAgent.error)
        )
      }

      "have a submit button" in {
        agentPage.submitButton mustBe Some(Expected.button)
      }
    }
  }

}
