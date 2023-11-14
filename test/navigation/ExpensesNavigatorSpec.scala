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

package navigation

import base.SpecBase
import controllers.journeys.expenses.goodsToSellOrUse.routes._
import controllers.journeys.expenses.officeSupplies.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.journeys.ExpensesGoodsToSellOrUse
import pages._
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}

class ExpensesNavigatorSpec extends SpecBase {

  val navigator = new ExpensesNavigator

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "OfficeSupplies journey" - {
        "must go from the OfficeSuppliesAmountPage to the OfficeSuppliesDisallowableAmountController" in {
          val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)

          navigator.nextPage(OfficeSuppliesAmountPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe expectedResult
        }

        "must go from the OfficeSuppliesDisallowableAmountPage to the OfficeSuppliesCYAController" in {
          val expectedResult = OfficeSuppliesCYAController.onPageLoad()

          navigator.nextPage(OfficeSuppliesDisallowableAmountPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe expectedResult
        }
      }

      "GoodsToSellOrUse journey" - {
        "must go from the Goods To Sell Or Use Amount page to the Disallowable Goods To Sell Or Use Amount page" in {

          navigator.nextPage(GoodsToSellOrUseAmountPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }

        "must go from the Disallowable Goods To Sell Or Use Amount page to the Goods To Sell Or Use CYA page" in {

          navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)
        }

        "must go from the Goods To Sell Or Use CYA page to the Section Completed page with ExpensesGoodsToSellOrUse journey" in {

          navigator.nextPage(GoodsToSellOrUseCYAPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            SectionCompletedStateController.onPageLoad(taxYear, stubbedBusinessId, ExpensesGoodsToSellOrUse.toString, NormalMode)
        }
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }
  }

}
