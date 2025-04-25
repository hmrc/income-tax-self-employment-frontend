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

import forms.expenses.travelAndAccommodation.{TravelForWorkYourMileageFormProvider, TravelForWorkYourVehicleFormProvider}
import models.common.UserType
import models.{Index, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.{TravelForWorkYourMileageView, TravelForWorkYourVehicleView}

class TravelForWorkYourMileageViewSpec extends ViewBaseSpec {

  private val view         = app.injector.instanceOf[TravelForWorkYourMileageView]
  private val formProvider = app.injector.instanceOf[TravelForWorkYourMileageFormProvider]
  private val index        = Index(1)
  private val vehicleName  = "Car"

  def form(userType: UserType): Form[BigDecimal] = formProvider(userType, vehicleName)

  def view(userType: UserType, mode: Mode, form: Form[BigDecimal]): Document = Jsoup.parse(
    view(form, mode, userType, taxYear, businessId, vehicleName, index)(fakeRequest, messages).body
  )

  object Expected {
    val heading = "Your work mileage"
    val p1      = "Tell us how many miles you travelled in Car during the tax year and we’ll work out the flat rate amount you can claim."
    val label   = "How many miles did you travel in Car?"
    val error   = "Enter the number of work miles you travelled in Car"
    val button  = "Continue"
  }

  object ExpectedAgent {
    val heading = "Your client’s work mileage"
    val p1 = "Tell us how many miles your client travelled in Car during the tax year and we’ll work out the flat rate amount your client can claim."
    val label = "How many miles did your client travel in Car?"
    val error = "Enter the number of work miles your client travelled in Car"
  }

  "The TravelForWorkYourMileageViewSpec" when {
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
