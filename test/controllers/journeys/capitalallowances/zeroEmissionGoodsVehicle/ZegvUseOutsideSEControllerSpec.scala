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
import base.SpecBase.{ToFutureOps, businessId, emptyUserAnswers, taxYear}
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSE
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.OptionValues._
import org.scalatest.TryValues._
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle.{ZegvTotalCostOfVehiclePage, ZegvUseOutsideSEPage}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, PlayRunners}

class ZegvUseOutsideSEControllerSpec extends AnyWordSpecLike with PlayRunners with ControllerTestScenarioSpec {
  lazy val getOnPageLoadNormal: String = routes.ZegvUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val postOnSubmitNormal: String  = routes.ZegvUseOutsideSEController.onSubmit(taxYear, businessId, NormalMode).url

  "onPaGeLoad" should {
    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, getOnPageLoadNormal)

    "render the correct view" in new TestScenario(
      UserType.Individual,
      emptyUserAnswers.set(ZegvTotalCostOfVehiclePage, BigDecimal(100.0), businessId.some).success.value.some
    ) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === OK)
        assert(getTitle(result) === "How much did you use the vehicle outside your self-employment? - income-tax-self-employment-frontend - GOV.UK")
      }
    }
  }

  "onSubmit" should {
    def request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, postOnSubmitNormal)
      .withFormUrlEncodedBody(("radioPercentage", "25%"), ("optDifferentAmount", "25"))

    mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns emptyUserAnswers.asFuture

    "render the correct view" in new TestScenario(
      UserType.Individual,
      emptyUserAnswers.set(ZegvUseOutsideSEPage, ZegvUseOutsideSE.Fifty, businessId.some).success.value.some
    ) {
      running(application) {
        val result = route(application, request).value
        assert(status(result) === SEE_OTHER)
        assert(redirectLocation(result).value === routes.ZegvHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, mode).url)
      }
    }
  }

}
