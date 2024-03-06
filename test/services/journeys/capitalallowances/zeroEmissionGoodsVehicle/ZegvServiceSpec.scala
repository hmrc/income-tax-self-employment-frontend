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

package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase.{businessId, convertScalaFuture, emptyUserAnswers, fakeDataRequest, userAnswersId}
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE}
import org.scalatest.TryValues._
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import queries.Settable.SetAnswer
import services.SelfEmploymentServiceImpl
import stubs.connectors.SelfEmploymentConnectorStub
import stubs.repositories.StubSessionRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ZegvServiceSpec extends AnyWordSpecLike {
  val existingAllAnswers = SetAnswer
    .setMany(businessId, emptyUserAnswers)(
      SetAnswer(ZegvAllowancePage, true),
      SetAnswer(ZegvClaimAmountPage, BigDecimal(200)),
      SetAnswer(ZegvHowMuchDoYouWantToClaimPage, ZegvHowMuchDoYouWantToClaim.LowerAmount),
      SetAnswer(ZegvOnlyForSelfEmploymentPage, true),
      SetAnswer(ZegvTotalCostOfVehiclePage, BigDecimal(100)),
      SetAnswer(ZegvUseOutsideSEPage, ZegvUseOutsideSE.Ten),
      SetAnswer(ZegvUseOutsideSEPercentagePage, 300)
    )
    .success
    .value

  val fakeRequest = fakeDataRequest(existingAllAnswers)
  val repository  = StubSessionRepository()
  val serviceStub = new SelfEmploymentServiceImpl(SelfEmploymentConnectorStub(), repository)
  val underTest   = new ZegvService(serviceStub)

  "submitAnswer" should {
    "return UserAnswers with cleared dependent pages when selected No" in {
      val updatedAnswers =
        underTest.submitAnswerAndClearDependentAnswers(ZeroEmissionGoodsVehiclePage, businessId, fakeRequest, newAnswer = false).futureValue

      val expectedAnswers = emptyUserAnswers
        .set(
          ZeroEmissionGoodsVehiclePage,
          false,
          Some(businessId)
        )
        .success
        .value
        .data

      assert(updatedAnswers.data === expectedAnswers)
      val dbAnswers = repository.state(userAnswersId).data
      assert(dbAnswers === expectedAnswers)
    }
  }
}
