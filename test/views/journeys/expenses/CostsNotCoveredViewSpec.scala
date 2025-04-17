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
import forms.standard.CurrencyFormProvider
import models.{Index, Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.CostsNotCoveredPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import views.html.journeys.expenses.travelAndAccommodation.CostsNotCoveredView

class CostsNotCoveredViewSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: CostsNotCoveredView = application.injector.instanceOf[CostsNotCoveredView]

  def createView(form: Form[_], mode: Mode, userType: UserType, taxYear: TaxYear, businessId: BusinessId)(implicit request: Request[_]): Html =
    view(form, mode, userType, taxYear, businessId, Index(-1))(request, messages)

  "CostsNotCoveredView" - {
    Seq(UserType.Individual, UserType.Agent).foreach { userType =>
      s"when user is $userType" - {
        "must return OK and the correct view for a GET" in {

          val formProvider = new CurrencyFormProvider()
          val form: Form[BigDecimal] = formProvider(
            CostsNotCoveredPage,
            userType,
            minValueError = s"costsNotCovered.error.lessThanZero.$userType",
            maxValueError = s"costsNotCovered.error.overMax.$userType",
            nonNumericError = s"costsNotCovered.error.nonNumeric.$userType"
          )

          val request = FakeRequest(GET, "/")
          val result  = createView(form, NormalMode, userType, taxYear, businessId)(request)

          contentAsString(result) mustEqual view(form, NormalMode, userType, taxYear, businessId, Index(-1))(request, messages(application)).toString
        }
      }
    }
  }

}
