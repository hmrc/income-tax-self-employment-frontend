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

import base.SpecBase
import forms.expenses.travelAndAccommodation.AddAnotherVehicleFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import builders.OneColumnSummaryBuilder._
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import viewmodels.components.OneColumnSummaryRow
import views.html.journeys.expenses.travelAndAccommodation.AddAnotherVehicleView

class AddAnotherVehicleViewSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: AddAnotherVehicleView = application.injector.instanceOf[AddAnotherVehicleView]

  def createView(form: Form[_], mode: Mode, vehicles: List[OneColumnSummaryRow], userType: UserType, taxYear: TaxYear, businessId: BusinessId)(
      implicit request: Request[_]): Html =
    view(form, mode, vehicles, userType, taxYear, businessId)(request, messages)

  "AddAnotherVehicleView" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {

          val formProvider        = new AddAnotherVehicleFormProvider()
          val form: Form[Boolean] = formProvider(userType)

          val request = FakeRequest(GET, "/")
          val result  = createView(form, NormalMode, testVehicles, userType, taxYear, businessId)(request)

          contentAsString(result) mustEqual view(form, NormalMode, testVehicles, userType, taxYear, businessId)(
            request,
            messages(application)).toString
        }
      }
    }
  }
}
