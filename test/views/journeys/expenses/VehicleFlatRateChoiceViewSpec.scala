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

import forms.expenses.travelAndAccommodation.VehicleFlatRateChoiceFormProvider
import models.common.UserType
import models.{Index, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.VehicleFlatRateChoiceView

class VehicleFlatRateChoiceViewSpec extends ViewBaseSpec {

  val application: Application        = new GuiceApplicationBuilder().build()
  val view: VehicleFlatRateChoiceView = application.injector.instanceOf[VehicleFlatRateChoiceView]

  private val formProvider    = app.injector.instanceOf[VehicleFlatRateChoiceFormProvider]
  private val index: Index    = Index(1)
  private val vehicle: String = "Car"

  def form(userType: UserType): Form[Boolean] = formProvider(vehicle, userType)

  def view(userType: UserType, mode: Mode, form: Form[Boolean]): Document =
    Jsoup.parse(
      view(form, vehicle, userType, taxYear, businessId, index, mode)(fakeRequest, messages).body
    )

  object Expected {
    val heading    = "You can choose how to claim your expenses for Car"
    val p1         = "If you know your mileage, we can help you calculate a flat rate for your vehicle expenses."
    val p2         = "You can then decide if you want to go ahead with the flat rate or claim actual expenses."
    val p3         = "If you have records of your actual expenses, you may choose to skip this step."
    val subHeading = "Do you want to calculate a flat rate?"
    val error      = "Select yes if you want to calculate a flat rate"
    val button     = "Continue"
  }

  object ExpectedAgent {
    val heading    = "Your client can choose how to claim their expenses for Car"
    val p1         = "If you know your client’s mileage, we can help you calculate a flat rate for their vehicle expenses."
    val p2         = "Your client can then decide if they want to go ahead with the flat rate or claim actual expenses."
    val p3         = "If you have records of your client’s actual expenses, you may choose to skip this step."
    val subHeading = "Does your client want to calculate a flat rate?"
    val error      = "Select yes if your client wants to calculate a flat rate"
  }

  "The VehicleFlatRateChoiceViewSpec" when {
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

      "have the correct message for paragraph 3" in {
        individualPage.para(3) mustBe Some(Expected.p3)
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

      "have the correct message for paragraph 3" in {
        agentPage.para(3) mustBe Some(ExpectedAgent.p3)
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
