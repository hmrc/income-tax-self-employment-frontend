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
import forms.expenses.travelAndAccommodation.TravelAndAccommodationFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.test.Helpers.contentAsString
import play.twirl.api.Html
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypeView

class TravelAndAccommodationExpenseTypeViewSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: TravelAndAccommodationExpenseTypeView = application.injector.instanceOf[TravelAndAccommodationExpenseTypeView]

  def createView(form: Form[_], mode: Mode, userType: UserType, taxYear: TaxYear, businessId: BusinessId)(implicit request: Request[_]): Html =
    view(form, mode, userType, taxYear, businessId)(request, messages)

  "TravelAndAccommodationExpenseTypeView" - {

    "render the view" in {
      val userType: UserType = UserType.Individual

      val formProvider = new TravelAndAccommodationFormProvider()

      val form: Form[Set[TravelAndAccommodationExpenseType]] = formProvider(UserType.Individual)

      val request = FakeRequest(GET, "/")
      val result  = createView(form, NormalMode, userType, taxYear, businessId)(request)

      contentAsString(result) mustEqual view(form, NormalMode, userType, taxYear, businessId)(request, messages(application)).toString
    }
  }

}
