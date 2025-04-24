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

import models.common.UserType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import views.ViewBaseSpec
import views.html.journeys.expenses.travelAndAccommodation.UseSimplifiedExpensesView

class UseSimplifiedExpensesViewSpec extends ViewBaseSpec {

  val application: Application        = new GuiceApplicationBuilder().build()
  val view: UseSimplifiedExpensesView = application.injector.instanceOf[UseSimplifiedExpensesView]

  private val vehicle: String = "Car"

  def view(userType: UserType): Document =
    Jsoup.parse(
      view(userType, vehicle, "")(fakeRequest, messages).body
    )

  object Expected {
    val heading = "You have to use simplified expenses for Car"
    val info    = "This is because you’ve already used simplified expenses for this vehicle."
    val button  = "Continue"
  }

  object ExpectedAgent {
    val heading = "Your client has to use simplified expenses for Car"
    val info    = "This is because your client has already used simplified expenses for this vehicle."
  }

  "The UseSimplifiedExpensesViewSpec" when {
    "the user is an Individual" must {
      val individualPage = view(UserType.Individual)

      "have the correct title" in {
        individualPage.title must include(Expected.heading)
      }

      "have the correct heading" in {
        individualPage.heading mustBe Some(Expected.heading)
      }

      "have the correct message for paragraph 1" in {
        individualPage.para(1) mustBe Some(Expected.info)
      }
      "have a submit button" in {
        individualPage.submitButton mustBe Some(Expected.button)
      }
    }

    "the user is an Agent" must {
      val agentPage = view(UserType.Agent)

      "have the correct title" in {
        agentPage.title must include(ExpectedAgent.heading)
      }

      "have the correct heading" in {
        agentPage.heading mustBe Some(ExpectedAgent.heading)
      }

      "have the correct message for paragraph 1" in {
        agentPage.para(1) mustBe Some(ExpectedAgent.info)
      }

      "have a submit button" in {
        agentPage.submitButton mustBe Some(Expected.button)
      }
    }
  }

}
