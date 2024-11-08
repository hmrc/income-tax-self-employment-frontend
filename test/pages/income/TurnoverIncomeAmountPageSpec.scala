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

package pages.income

import base.SpecBase._
import org.scalatest.wordspec.AnyWordSpecLike

class TurnoverIncomeAmountPageSpec extends AnyWordSpecLike {

  "navigation" should {
    "navigate to check your answers page on valid answer" in {
      val answers = emptyUserAnswers
        .set(TurnoverIncomeAmountPage, BigDecimal(1000), Some(businessId))
        .success
        .value
        .set(NonTurnoverIncomeAmountPage, BigDecimal(1000), Some(businessId))
        .success
        .value
      val result = TurnoverIncomeAmountPage.cyaPage(answers, taxYear, businessId)
      assert(result.url.endsWith(s"/$taxYear/SJPR05893938418/income/check-your-income"))
    }

    "navigate to expenses warning page when total turnover and non turnover is more then 85000" in {
      val answers = emptyUserAnswers
        .set(TurnoverIncomeAmountPage, BigDecimal(5000), Some(businessId))
        .success
        .value
        .set(NonTurnoverIncomeAmountPage, BigDecimal(85000), Some(businessId))
        .success
        .value
      val result = TurnoverIncomeAmountPage.cyaPage(answers, taxYear, businessId)
      assert(result.url.endsWith(s"/$taxYear/SJPR05893938418/income/expenses-warning"))
    }

    "navigate to expenses warning page when turnover total is more then 85000" in {
      val answers = emptyUserAnswers
        .set(TurnoverIncomeAmountPage, BigDecimal(85000), Some(businessId))
        .success
        .value

      val result = NonTurnoverIncomeAmountPage.cyaPage(answers, taxYear, businessId)
      assert(result.url.endsWith(s"/$taxYear/SJPR05893938418/income/expenses-warning"))
    }

    "navigate to expenses warning page when total turnover and non turnover is equal to 85000" in {
      val answers = emptyUserAnswers
        .set(TurnoverIncomeAmountPage, BigDecimal(5000), Some(businessId))
        .success
        .value
        .set(NonTurnoverIncomeAmountPage, BigDecimal(80000), Some(businessId))
        .success
        .value
      val result = TurnoverIncomeAmountPage.cyaPage(answers, taxYear, businessId)
      assert(result.url.endsWith(s"/$taxYear/SJPR05893938418/income/expenses-warning"))
    }
  }
}
