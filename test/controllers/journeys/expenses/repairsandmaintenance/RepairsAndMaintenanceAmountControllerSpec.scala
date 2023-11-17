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

import base.SpecBase
import base.SpecBase._
import controllers.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountControllerSpec._
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import gens._
import models.common.{AccountingType, Language, UserType, onwardRoute}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers._
import play.api.test.{FakeRequest, PlayRunners}
import services.SelfEmploymentServiceBase
import stubs.services.SelfEmploymentServiceStub
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

class RepairsAndMaintenanceAmountControllerSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with PlayRunners with OptionValues {
  private val validAnswer   = BigDecimal(100.00)
  private val invalidAnswer = "invalid value"

  "onPageLoad" should {
    "return OK and render view" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application = buildApp(accountingType, userType)
        val form        = new RepairsAndMaintenanceAmountFormProvider()(userType)

        val routeUnderTest = routes.RepairsAndMaintenanceAmountController.onPageLoad(currTaxYear, stubBusinessId, mode).url
        val getRequest     = FakeRequest(GET, routeUnderTest)
        val view           = application.injector.instanceOf[RepairsAndMaintenanceAmountView]
        val expectedView =
          view(form, mode, userType, currTaxYear, stubBusinessId, accountingType)(getRequest, messages(application, Language.English))
            .toString()

        val result = route(application, getRequest).value

        status(result) mustBe OK
        contentAsString(result) mustEqual expectedView
      }
    }
  }

  "onSubmit" should {
    "redirect to next when valid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType)
        val routeUnderTest = routes.RepairsAndMaintenanceAmountController.onSubmit(currTaxYear, stubBusinessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, postRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return bad request when invalid data is submitted" in {
      forAll(userTypeGen, accountingTypeGen, modeGen) { (userType, accountingType, mode) =>
        val application    = buildApp(accountingType, userType)
        val routeUnderTest = routes.RepairsAndMaintenanceAmountController.onSubmit(currTaxYear, stubBusinessId, mode).url
        val postRequest    = FakeRequest(POST, routeUnderTest).withFormUrlEncodedBody(("value", invalidAnswer))
        val view           = application.injector.instanceOf[RepairsAndMaintenanceAmountView]
        val form           = new RepairsAndMaintenanceAmountFormProvider()(userType).bind(Map("value" -> invalidAnswer))
        val expectedView =
          view(form, mode, userType, currTaxYear, stubBusinessId, accountingType)(postRequest, messages(application, Language.English))
            .toString()

        val result = route(application, postRequest).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustEqual expectedView
      }
    }

  }

}

object RepairsAndMaintenanceAmountControllerSpec {

  def buildApp(accountingType: AccountingType, userType: UserType): Application = {
    val selfEmploymentService = SelfEmploymentServiceStub(Right(accountingType), emptyUserAnswers)
    SpecBase
      .applicationBuilder(None, userType)
      .overrides(
        bind[SelfEmploymentServiceBase].toInstance(selfEmploymentService),
        bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
      )
      .build()
  }

}
