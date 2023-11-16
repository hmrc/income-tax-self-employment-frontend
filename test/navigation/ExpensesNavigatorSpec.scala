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
import models.database.UserAnswers
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import play.api.libs.json.Json

class ExpensesNavigatorSpec extends SpecBase {

  val navigator = new ExpensesNavigator

  case object UnknownPage extends Page

  "ExpensesNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode
        "the page is OfficeSuppliesAmountPage" - {
          "some expenses were claimed to be disallowable" - {
            "navigate to the OfficeSuppliesDisallowableAmountController" in {
              val data        = Json.obj(stubbedBusinessId -> Json.obj("officeSupplies" -> "yesDisallowable"))
              val userAnswers = UserAnswers(userAnswersId, data)

              val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, stubbedBusinessId, mode)

              navigator.nextPage(OfficeSuppliesAmountPage, mode, userAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "all expenses were claimed as allowable" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val data        = Json.obj(stubbedBusinessId -> Json.obj("officeSupplies" -> "yesAllowable"))
              val userAnswers = UserAnswers(userAnswersId, data)

              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, userAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "the page is OfficeSuppliesDisallowableAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "the page is GoodsToSellOrUseAmountPage" - {
            "navigate to the DisallowableGoodsToSellOrUseAmountController" in {
              val expectedResult = DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)

              navigator.nextPage(GoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "the page is DisallowableGoodsToSellOrUseAmountPage" - {
            "navigate to the GoodsToSellOrUseCYAController" in {
              val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "the page is GoodsToSellOrUseCYAPage" - {
            "navigate to the SectionCompletedStateController" in {
              val expectedResult = SectionCompletedStateController.onPageLoad(taxYear, stubbedBusinessId, ExpensesGoodsToSellOrUse.toString, mode)

              navigator.nextPage(GoodsToSellOrUseCYAPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "page does not exist" - {
            "navigate to the JourneyRecoveryController" in {
              val expectedResult = JourneyRecoveryController.onPageLoad()

              navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
        }
        "in CheckMode" - {
          val mode = CheckMode
          "the page is OfficeSuppliesAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(OfficeSuppliesAmountPage, CheckMode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "the page is OfficeSuppliesDisallowableAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, CheckMode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
          "page does not exist" - {
            "navigate to the JourneyRecoveryController" in {
              val expectedResult = JourneyRecoveryController.onPageLoad()

              navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, stubbedBusinessId) shouldBe expectedResult
            }
          }
        }
      }
    }
  }

}
