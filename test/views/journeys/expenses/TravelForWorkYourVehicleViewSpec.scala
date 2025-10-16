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

import forms.expenses.travelAndAccommodation.TravelForWorkYourVehicleFormProvider
import models.common.UserType
import models.{Index, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.TravelForWorkYourVehicleView

class TravelForWorkYourVehicleViewSpec extends ViewBaseSpec {

  private val view         = app.injector.instanceOf[TravelForWorkYourVehicleView]
  private val formProvider = app.injector.instanceOf[TravelForWorkYourVehicleFormProvider]
  private val index        = Index(1)

  def form(userType: UserType): Form[String] = formProvider(userType)

  def view(userType: UserType, mode: Mode, form: Form[String]): Document = Jsoup.parse(
    view(form, mode, userType, taxYear, businessId, index)(fakeRequest, messages).body
  )

  object Expected {
    val heading = "Your vehicle"
    val p1      = "If you had more than one vehicle, you can tell us about others later."
    val p2      = "You cannot use your registration number. You should use something else, for example:"
    val li1     = "Work van"
    val li2     = "Blue Skoda"
    val li3     = "Rental car"
    val label   = "How do you want to identify your vehicle?"
    val error   = "Enter a name to identify your vehicle"
    val button  = "Continue"
  }

  object ExpectedAgent {
    val heading = "Your client’s vehicle"
    val p1      = "If your client had more than one vehicle, you can tell us about others later."
    val p2      = "You cannot use your client’s registration number. You should use something else, for example:"
    val label   = "How do you want to identify your client’s vehicle?"
    val error   = "Enter a name to identify your client’s vehicle"
  }

  "The TravelForWorkYourVehicleView" when {
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

      "have the correct list of bullets" in {
        individualPage.unorderedList(1) mustBe List(Expected.li1, Expected.li2, Expected.li3)
      }

      "have a single text input with the correct label" in {
        individualPage.textBox("value") mustBe Some(Expected.label)
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

      "have the correct list of bullets" in {
        agentPage.unorderedList(1) mustBe List(Expected.li1, Expected.li2, Expected.li3)
      }

      "have a single text input with the correct label" in {
        agentPage.textBox("value") mustBe Some(ExpectedAgent.label)
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
