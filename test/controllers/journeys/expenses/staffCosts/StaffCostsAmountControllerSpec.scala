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

package controllers.journeys.expenses.staffCosts

import base.SpecBase._
import common.TestApp._
import forms.expenses.staffCosts.StaffCostsAmountFormProvider
import gens._
import models.common.{Language, onwardRoute}
import models.journeys.expenses.DisallowableStaffCosts
import models.journeys.expenses.DisallowableStaffCosts.{No, Yes}
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.expenses.tailoring.DisallowableStaffCostsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.staffCosts.StaffCostsAmountView

class StaffCostsAmountControllerSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {
  private val validAnswer                          = BigDecimal(100.00)
  private val invalidAnswer                        = "invalid value"
  val disallowableGen: Gen[DisallowableStaffCosts] = Gen.oneOf(Yes, No)

  "onPageLoad" should {
    "return OK and render view" in {
      forAll(userTypeGen, modeGen, disallowableGen) { (userType, mode, disallowable) =>
        val userAnswers    = emptyUserAnswers.set(DisallowableStaffCostsPage, disallowable, Some(stubbedBusinessId)).success.value
        val application    = buildAppFromUserType(userType, Some(userAnswers))
        val form           = new StaffCostsAmountFormProvider()(userType)
        val routeUnderTest = routes.StaffCostsAmountController.onPageLoad(taxYear, businessId, mode).url
        val getRequest     = FakeRequest(GET, routeUnderTest)

        val result = route(application, getRequest).value

        status(result) mustBe OK

        val view = application.injector.instanceOf[StaffCostsAmountView]
        val expectedView =
          view(form, mode, userType, taxYear, businessId, disallowable)(getRequest, messages(application, Language.English))
            .toString()
        contentAsString(result) mustEqual expectedView
      }
    }
  }

  "onSubmit" should {
    "redirect to next when valid data is submitted" in {
      forAll(userTypeGen, modeGen) { (userType, mode) =>
        val userAnswers    = emptyUserAnswers.set(DisallowableStaffCostsPage, Yes, Some(stubbedBusinessId)).success.value
        val application    = buildAppFromUserType(userType, Some(userAnswers))
        val routeUnderTest = routes.StaffCostsAmountController.onSubmit(taxYear, businessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return bad request when invalid data is submitted" in {
      forAll(userTypeGen, modeGen, disallowableGen) { (userType, mode, disallowable) =>
        val userAnswers    = emptyUserAnswers.set(DisallowableStaffCostsPage, disallowable, Some(stubbedBusinessId)).success.value
        val application    = buildAppFromUserType(userType, Some(userAnswers))
        val routeUnderTest = routes.StaffCostsAmountController.onSubmit(taxYear, businessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", invalidAnswer))
        val view           = application.injector.instanceOf[StaffCostsAmountView]
        val form           = new StaffCostsAmountFormProvider()(userType).bind(Map("value" -> invalidAnswer))
        val expectedView =
          view(form, mode, userType, taxYear, businessId, disallowable)(postRequest, messages(application, Language.English))
            .toString()

        val result = route(application, postRequest).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustEqual expectedView
      }
    }

  }

}
