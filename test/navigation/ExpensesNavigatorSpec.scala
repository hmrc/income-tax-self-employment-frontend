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
import controllers.journeys.expenses.entertainment.routes._
import controllers.journeys.expenses.goodsToSellOrUse.routes._
import controllers.journeys.expenses.officeSupplies.routes._
import controllers.journeys.expenses.staffCosts
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.common.AccountingType.{Accrual, Cash}
import models.database.UserAnswers
import models.journeys.Journey.{ExpensesEntertainment, ExpensesGoodsToSellOrUse, ExpensesOfficeSupplies, ExpensesStaffCosts}
import models.journeys.expenses.DisallowableStaffCosts
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.entertainment.{EntertainmentAmountPage, EntertainmentCYAPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesCYAPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsCYAPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.DisallowableStaffCostsPage
import play.api.libs.json.Json

class ExpensesNavigatorSpec extends SpecBase {

  val navigator = new ExpensesNavigator

  case object UnknownPage extends Page

  "ExpensesNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        "OfficeSupplies journey" - {
          "the page is OfficeSuppliesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the OfficeSuppliesDisallowableAmountController" in {
                val data        = Json.obj(stubbedBusinessId -> Json.obj("officeSupplies" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, stubbedBusinessId, mode)

                navigator.nextPage(OfficeSuppliesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the OfficeSuppliesCYAController" in {
                val data        = Json.obj(stubbedBusinessId -> Json.obj("officeSupplies" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

                navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is OfficeSuppliesDisallowableAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the page is OfficeSuppliesCYAPage" - {
            "navigate to the SectionCompletedStateController" in {
              val expectedResult = SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesOfficeSupplies.toString, mode)

              navigator.nextPage(OfficeSuppliesCYAPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "GoodsToSellOrUse journey" - {
          "the page is GoodsToSellOrUseAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the DisallowableGoodsToSellOrUseAmountController" in {
                val data        = Json.obj(stubbedBusinessId -> Json.obj("goodsToSellOrUse" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, stubbedBusinessId, mode)

                navigator.nextPage(GoodsToSellOrUseAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the GoodsToSellOrUseCYAController" in {
                val data        = Json.obj(stubbedBusinessId -> Json.obj("goodsToSellOrUse" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)

                navigator.nextPage(GoodsToSellOrUseAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is DisallowableGoodsToSellOrUseAmountPage" - {
            "navigate to the GoodsToSellOrUseCYAController" in {
              val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)

              navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the page is GoodsToSellOrUseCYAPage" - {
            "navigate to the SectionCompletedStateController" in {
              val expectedResult = SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesGoodsToSellOrUse.toString, mode)

              navigator.nextPage(GoodsToSellOrUseCYAPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "Entertainment journey" - {
          "the page is EntertainmentAmountPage" - {
            "navigate to the EntertainmentCYAController" in {
              val expectedResult = EntertainmentCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(EntertainmentAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the page is EntertainmentCYAPage" - {
            "navigate to the SectionCompletedStateController" in {
              val expectedResult = SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesEntertainment.toString, mode)

              navigator.nextPage(EntertainmentCYAPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "StaffCosts journey" - {
          "the page is StaffCostsAmountPage" - {
            "should navigate to the StaffCostsDisallowableAmountController" - {
              "when expenses are disallowable and accounting type is ACCRUAL" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, DisallowableStaffCosts.Yes, Some(businessId)).success.value

                val expectedResult =
                  staffCosts.routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId, Some(Accrual)) mustBe expectedResult
              }
            }
            "should navigate to the StaffCostsCYAController" - {
              "when expenses are not disallowable" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, DisallowableStaffCosts.No, Some(businessId)).success.value

                val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId, Some(Accrual)) mustBe expectedResult
              }
              "when accounting type is CASH" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, DisallowableStaffCosts.Yes, Some(businessId)).success.value

                val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId, Some(Cash)) mustBe expectedResult
              }
            }
          }
          "the page is StaffCostsDisallowableAmountController" - {
            "navigate to the StaffCostsCYAController" in {
              val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(StaffCostsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the page is StaffCostsCYAPage" - {
            "navigate to the SectionCompletedStateController" in {
              val expectedResult = SectionCompletedStateController.onPageLoad(
                taxYear,
                businessId,
                ExpensesStaffCosts.toString,
                mode
              )

              navigator.nextPage(StaffCostsCYAPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }

      "in CheckMode" - {
        val mode = CheckMode

        "the page is OfficeSuppliesAmountPage" - {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

            navigator.nextPage(OfficeSuppliesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OfficeSuppliesDisallowableAmountPage" - {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId)

            navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is GoodsToSellOrUseAmountPage" - {
          "navigate to the GoodsToSellOrUseCYAController" in {
            val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)

            navigator.nextPage(GoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is DisallowableGoodsToSellOrUseAmountPage" - {
          "navigate to the GoodsToSellOrUseCYAController" in {
            val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, stubbedBusinessId)

            navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is EntertainmentAmountPage" - {
          "navigate to the EntertainmentCYAController" in {
            val expectedResult = EntertainmentCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(EntertainmentAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is StaffCostsAmountPage" - {
          "navigate to the StaffCostsCYAController" in {
            val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(StaffCostsAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is StaffCostsDisallowableAmountPage" - {
          "navigate to the StaffCostsCYAController" in {
            val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(StaffCostsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }
  }

}
