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
import forms.expenses.staffCosts._
import gens._
import models.common.{Language, TextAmount, onwardRoute}
import models.database.UserAnswers
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.expenses.staffCosts._

class StaffCostsDisallowableAmountControllerSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {
  private val validAnswer   = BigDecimal(200.00)
  private val invalidAnswer = "invalid value"
  private val data          = Json.obj(stubbedBusinessId -> Json.obj("staffCostsAmount" -> validAnswer))
  private val userAnswers   = UserAnswers(userAnswersId, data)

  "onPageLoad" should {
    "return OK and render view" in {
      forAll(userTypeGen, modeGen) { (userType, mode) =>
        val application    = buildAppFromUserType(userType, Some(userAnswers))
        val routeUnderTest = routes.StaffCostsDisallowableAmountController.onPageLoad(currTaxYear, stubBusinessId, mode).url
        val getRequest     = FakeRequest(GET, routeUnderTest)

        val result = route(application, getRequest).value

        status(result) mustBe OK

        val view         = application.injector.instanceOf[StaffCostsDisallowableAmountView]
        val msg          = messages(application, Language.English)
        val form         = new StaffCostsDisallowableAmountFormProvider()(userType.toString, validAnswer)
        val expectedView = view(form, mode, userType, currTaxYear, stubBusinessId, TextAmount(validAnswer))(getRequest, msg).toString()
        contentAsString(result) mustEqual expectedView
      }
    }
  }

  "onSubmit" should {
    "redirect to next when valid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType, Some(userAnswers))
        val routeUnderTest = routes.StaffCostsDisallowableAmountController.onSubmit(currTaxYear, stubBusinessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return bad request when invalid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType, Some(userAnswers))
        val routeUnderTest = routes.StaffCostsDisallowableAmountController.onSubmit(currTaxYear, stubBusinessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", invalidAnswer))

        val result = route(application, postRequest).value

        status(result) mustBe BAD_REQUEST

        val view = application.injector.instanceOf[StaffCostsDisallowableAmountView]
        val form = new StaffCostsDisallowableAmountFormProvider()(userType.toString, validAnswer).bind(Map("value" -> invalidAnswer))
        val expectedView =
          view(form, mode, userType, currTaxYear, stubBusinessId, TextAmount(validAnswer))(postRequest, messages(application, Language.English))
            .toString()
        contentAsString(result) mustEqual expectedView
      }
    }

  }

}
