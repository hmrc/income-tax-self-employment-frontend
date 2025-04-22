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

import forms.expenses.travelAndAccommodation.VehicleTypeFormProvider
import models.journeys.expenses.travelAndAccommodation.VehicleType
import models.{Index, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.VehicleTypeView

class VehicleTypeViewSpec extends ViewBaseSpec {

  val application: Application = new GuiceApplicationBuilder().build()
  val view: VehicleTypeView    = application.injector.instanceOf[VehicleTypeView]

  private val formProvider    = app.injector.instanceOf[VehicleTypeFormProvider]
  private val index: Index    = Index(1)
  private val vehicle: String = "Car"

  def form: Form[VehicleType] = formProvider(vehicle)

  def view(mode: Mode, form: Form[VehicleType]): Document =
    Jsoup.parse(
      view(form, vehicle, taxYear, businessId, index, mode)(fakeRequest, messages).body
    )

  object Expected {
    val heading = "What kind of vehicle is Car?"
    val error   = "Select what kind of vehicle Car is"
    val button  = "Continue"
  }

  "The SimplifiedExpensesViewSpec" when {
    "the user is an Individual" must {
      val individualPage = view(NormalMode, form)

      "have the correct title" in {
        individualPage.title must include(Expected.heading)
      }

      "have the correct heading" in {
        individualPage.heading mustBe Some(Expected.heading)
      }

      "display correct boolean label" in {
        individualPage.radio("CarOrGoodsVehicle") mustBe Some("Car or goods vehicle")
        individualPage.radio("Motorcycle") mustBe Some("Motorcycle")
      }

      "show the mandatory field error and error summary when the form is blank" in {
        val formWithErrors = form.bind(Map("value" -> ""))
        val individualPage = view(NormalMode, formWithErrors)

        individualPage.errorSummaryLinks mustBe List(
          Link(href = "#value_0", text = Expected.error)
        )
      }

      "have a submit button" in {
        individualPage.submitButton mustBe Some(Expected.button)
      }
    }
  }

}
