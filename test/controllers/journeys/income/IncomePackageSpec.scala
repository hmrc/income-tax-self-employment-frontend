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

package controllers.journeys.income

import base.SpecBase
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.errors.ServiceError.NotFoundError
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.income.TurnoverIncomeAmountPage
import play.api.libs.json.Json

import scala.concurrent.Future

class IncomePackageSpec extends SpecBase {

  val maxIncomeTradingAllowance: BigDecimal = 1000
  val smallTurnover: BigDecimal             = 450.00
  val largeTurnover: BigDecimal             = 45000.00

  "getMaxTradingAllowance" - {
    "should return a BigDecimal trading allowance that is" - {
      "equal to the turnover amount when the turnover amount is less than the max trading allowance" in {
        val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, smallTurnover, Some(businessId)).success.value

        getMaxTradingAllowance(businessId, userAnswers) shouldBe smallTurnover.asRight
      }

      "equal to the max allowance when the turnover amount is equal or greater than the max trading allowance" in {
        val userAnswersLargeTurnover =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, largeTurnover, Some(businessId)).success.value
        val userAnswersEqualToMax =
          UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, maxIncomeTradingAllowance, Some(businessId)).success.value

        getMaxTradingAllowance(businessId, userAnswersLargeTurnover) shouldBe maxIncomeTradingAllowance.asRight
        getMaxTradingAllowance(businessId, userAnswersEqualToMax) shouldBe maxIncomeTradingAllowance.asRight
      }
    }
    "should return a ServiceError when there is no Turnover Income value" in {
      getMaxTradingAllowance(businessId, UserAnswers(userAnswersId)) shouldBe NotFoundError(
        s"TurnoverIncomeAmountPage value not found for business ID: ${businessId.value}").asLeft
    }
  }

  "returnOptionalTotalIncome" - {
    "should return total income inside of an option" in {
      val totalIncomeResult: ApiResultT[BigDecimal] = EitherT.rightT[Future, ServiceError](1000)
      val result                                    = returnOptionalTotalIncome(totalIncomeResult).value.futureValue

      result shouldEqual Some(1000).asRight
    }
    "should return a None if the result is Not Found" in {
      val totalIncomeResult: ApiResultT[BigDecimal] = EitherT.leftT[Future, BigDecimal](NotFoundError("NOT_FOUND"))
      val result                                    = returnOptionalTotalIncome(totalIncomeResult).value.futureValue

      result shouldEqual None.asRight
    }
  }

  "clearAllowancePagesData" - {
    val data        = Json.obj("howMuchTradingAllowance" -> "maximum", "tradingAllowanceAmount" -> 200)
    val userAnswers = buildUserAnswers(data)
    "should clear howMuchTradingAllowance and tradingAllowanceAmount page data from user answers when answers are changed" in {
      val result = clearAllowancePagesData(answerUnchanged = false, businessId, userAnswers)

      result.futureValue.data shouldBe Json.obj(businessId.value -> Json.obj())
    }
    "should return original user answers when answers are unchanged" in {
      val result = clearAllowancePagesData(answerUnchanged = true, businessId, userAnswers)

      result.futureValue.data shouldBe Json.obj(businessId.value -> data)
    }
  }
}
