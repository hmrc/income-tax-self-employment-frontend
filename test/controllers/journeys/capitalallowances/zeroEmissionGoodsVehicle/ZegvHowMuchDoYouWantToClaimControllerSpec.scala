/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.ControllerTestScenarioSpec
import base.SpecBase.{ToFutureOps, businessId, emptyUserAnswers, emptyUserAnswersAccrual, taxYear}
import cats.implicits.catsSyntaxOptionId
import controllers.standard.{routes => genRoutes}
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.OptionValues._
import org.scalatest.TryValues._
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvTotalCostOfVehiclePage
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, PlayRunners}

class ZegvHowMuchDoYouWantToClaimControllerSpec extends AnyWordSpecLike with PlayRunners with ControllerTestScenarioSpec {
  lazy val getOnPageLoadNormal: String = routes.ZegvHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val postOnSubmitNormal: String  = routes.ZegvHowMuchDoYouWantToClaimController.onSubmit(taxYear, businessId, NormalMode).url

  "onPaGeLoad" should {
    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, getOnPageLoadNormal)

    "return to the recovery page when no required data" in new TestScenario(UserType.Individual, emptyUserAnswersAccrual.some) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === SEE_OTHER)
        assert(redirectLocation(result).value === genRoutes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "render the correct view" in new TestScenario(
      UserType.Individual,
      emptyUserAnswers.set(ZegvTotalCostOfVehiclePage, BigDecimal(100.0), businessId.some).success.value.some
    ) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === OK)
        assert(getTitle(result) === "ZegvHowMuchDoYouWantToClaim.subHeading.individual - income-tax-self-employment-frontend - GOV.UK")
      }
    }
  }

  "onSubmit" should {
    def request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, postOnSubmitNormal)
      .withFormUrlEncodedBody(("howMuchDoYouWantToClaim", "fullCost"))

    mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns emptyUserAnswers.asFuture

    "return to the recovery page if no required data" in new TestScenario(UserType.Individual, emptyUserAnswersAccrual.some) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === SEE_OTHER)
        assert(redirectLocation(result).value === genRoutes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "render the correct view" in new TestScenario(
      UserType.Individual,
      emptyUserAnswers.set(ZegvTotalCostOfVehiclePage, BigDecimal(100.0), businessId.some).success.value.some
    ) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === SEE_OTHER)
        assert(redirectLocation(result).value === routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId).url)
      }
    }
  }
}
