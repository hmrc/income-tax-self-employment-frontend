/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys.expenses.repairsandmaintenance

import base.SpecBase._
import common.TestApp._
import forms.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountFormProvider, RepairsAndMaintenanceDisallowableAmountFormProvider}
import gens._
import models.common.{TextAmount, onwardRoute}
import models.database.UserAnswers
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountView, RepairsAndMaintenanceDisallowableAmountView}

class RepairsAndMaintenanceDisallowableAmountControllerSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {
  private val invalidAnswer   = "invalid value"
  private val allowableAmount = BigDecimal(200.00)
  private val data            = Json.obj(businessId.value -> Json.obj("repairsAndMaintenanceAmount" -> allowableAmount))
  private val userAnswers     = UserAnswers(userAnswersId, data)

  "onPageLoad" should {
    "return OK and render view" in {
      forAll(userTypeGen, modeGen) { (userType, mode) =>
        val application    = buildAppFromUserType(userType, Some(userAnswers))
        val routeUnderTest = routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, mode).url
        val getRequest     = FakeRequest(GET, routeUnderTest)

        val result = route(application, getRequest).value

        status(result) mustBe OK

        val view         = application.injector.instanceOf[RepairsAndMaintenanceDisallowableAmountView]
        val msg          = messages(application)
        val form         = new RepairsAndMaintenanceDisallowableAmountFormProvider()(userType, allowableAmount)(msg)
        val expectedView = view(form, mode, taxYear, businessId, userType, TextAmount(allowableAmount))(getRequest, msg).toString()
        contentAsString(result) mustEqual expectedView
      }
    }
  }

  "onSubmit" should {
    "redirect to next when valid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType)
        val routeUnderTest = routes.RepairsAndMaintenanceAmountController.onSubmit(taxYear, businessId, mode).url
        val validAnswer    = BigDecimal(100.00)
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return bad request when invalid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType)
        val routeUnderTest = routes.RepairsAndMaintenanceAmountController.onSubmit(taxYear, businessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", invalidAnswer))

        val result = route(application, postRequest).value

        status(result) mustBe BAD_REQUEST

        val view = application.injector.instanceOf[RepairsAndMaintenanceAmountView]
        val form = new RepairsAndMaintenanceAmountFormProvider()(userType).bind(Map("value" -> invalidAnswer))
        val expectedView =
          view(form, mode, userType, taxYear, businessId, accountingType)(postRequest, messages(application))
            .toString()
        contentAsString(result) mustEqual expectedView
      }
    }

  }

}
