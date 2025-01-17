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

package views.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsNewClaimAmountFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsNewClaimAmountView

class StructuresBuildingsNewClaimAmountViewSpec extends SpecBase with Matchers {

  val formProvider = new StructuresBuildingsNewClaimAmountFormProvider()
  val index: Int   = 0
  val mode: Mode   = NormalMode

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val view: StructuresBuildingsNewClaimAmountView = application.injector.instanceOf[StructuresBuildingsNewClaimAmountView]

  def createView(form: Form[_], mode: Mode, userType: UserType, taxYear: TaxYear, businessId: BusinessId, index: Int)(implicit
      request: Request[_]): Html =
    view(form, mode, userType, taxYear, businessId, index)(request, messages)

  "StructuresBuildingsNewClaimAmountView" - {

    "render correctly for Individual user type" in {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Individual)
      val result  = createView(form, mode, UserType.Individual, taxYear, businessId, index)(request)

      contentAsString(result) mustEqual view(form, mode, UserType.Individual, taxYear, businessId, index)(request, messages(application)).toString

    }

    "render correctly for Agent user type" in {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Agent)
      val result  = createView(form, mode, UserType.Agent, taxYear, businessId, index)(request)

      contentAsString(result) mustEqual view(form, mode, UserType.Agent, taxYear, businessId, index)(request, messages(application)).toString

    }

    "render error message for required field for Individual user type" in {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Individual).bind(Map("value" -> ""))
      val result  = createView(form, mode, UserType.Individual, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("structuresBuildingsNewClaimAmount.error.required.individual"))
    }

    "render error message for required field for Agent user type" ignore {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Agent).bind(Map("value" -> ""))
      val result  = createView(form, mode, UserType.Agent, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("structuresBuildingsNewClaimAmount.error.required.agent"))
    }

    "render error message for non-numeric input for Individual user type" ignore {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Individual).bind(Map("value" -> "abc"))
      val result  = createView(form, mode, UserType.Individual, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("error.nonNumeric.individual"))
    }

    "render error message for non-numeric input for Agent user type" ignore {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Agent).bind(Map("value" -> "abc"))
      val result  = createView(form, mode, UserType.Agent, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("error.nonNumeric.agent"))
    }

    "render error message for value less than zero for Agent user type" in {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Agent).bind(Map("value" -> "-1"))
      val result  = createView(form, mode, UserType.Agent, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("error.lessThanZero"))
    }

    "render error message for value over maximum for Agent user type" in {
      val request = FakeRequest(GET, "/")
      val form    = formProvider(UserType.Agent).bind(Map("value" -> "100000000000.01"))
      val result  = createView(form, mode, UserType.Agent, taxYear, businessId, index)(request)

      contentAsString(result) must include(messages("error.overMax"))
    }
  }

}
