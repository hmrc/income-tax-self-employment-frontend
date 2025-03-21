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

package controllers.journeys.expenses.travelAndAccommodation

import base.SpecBase
import models.common.UserType
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.travelAndAccommodation.UseSimplifiedExpensesView

class UseSimplifiedExpensesControllerSpec extends SpecBase {

  "UseSimplifiedExpenses Controller" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {

        "must return OK and the correct view for a GET" in {

          val ua = emptyUserAnswers
            .set(TravelForWorkYourVehiclePage, "CarName", Some(businessId))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(ua), userType = userType).build()

          running(application) {
            val request = FakeRequest(GET, routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[UseSimplifiedExpensesView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(userType, "CarName", "TODO")(request, messages(application)).toString
          }
        }

        "must redirect to 'there is a problem' page when TravelForWorkYourVehiclePage data is missing" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId).url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
