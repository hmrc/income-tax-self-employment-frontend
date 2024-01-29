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
import controllers.journeys.expenses._
import controllers.journeys.expenses.entertainment.routes._
import controllers.journeys.expenses.financialCharges.routes._
import controllers.journeys.expenses.goodsToSellOrUse.routes._
import controllers.journeys.expenses.officeSupplies.routes._
import controllers.journeys.expenses.otherExpenses.routes._
import controllers.standard.routes._
import models._
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.DisallowableStaffCosts
import models.journeys.expenses.workplaceRunningCosts.WfhFlatRateOrActualCosts
import models.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25Hours
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.DisallowableStaffCostsPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountPage
import pages.expenses.workplaceRunningCosts.workingFromHome.{MoreThan25HoursPage, WfhFlatRateOrActualCostsPage, WorkingFromHomeHoursPage}
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
                val data        = Json.obj(businessId.value -> Json.obj("officeSupplies" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(OfficeSuppliesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the OfficeSuppliesCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("officeSupplies" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is OfficeSuppliesDisallowableAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }
        "OtherExpenses journey" - {
          "the page is OtherExpensesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the OtherExpensesDisallowableAmountController" in {
                val data        = Json.obj(businessId.value -> Json.obj("otherExpenses" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(OtherExpensesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the OtherExpensesCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("otherExpenses" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is OtherExpensesDisallowableAmountPage" - {
            "navigate to the OtherExpensesCYAController" in {
              val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }
        "FinancialCharges journey" - {
          "the page is FinancialChargesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the FinancialChargesDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableOtherFinancialCharges" -> "yes"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(FinancialChargesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "no disallowable expenses" - {
              "navigate to the FinancialChargesCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableOtherFinancialCharges" -> "no"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = FinancialChargesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(FinancialChargesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is FinancialChargesDisallowableAmountPage" - {
            "navigate to the FinancialChargesCYAController" in {
              val expectedResult = FinancialChargesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(FinancialChargesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "GoodsToSellOrUse journey" - {
          "the page is GoodsToSellOrUseAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the DisallowableGoodsToSellOrUseAmountController" in {
                val data        = Json.obj(businessId.value -> Json.obj("goodsToSellOrUse" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(GoodsToSellOrUseAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the GoodsToSellOrUseCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("goodsToSellOrUse" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(GoodsToSellOrUseAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is DisallowableGoodsToSellOrUseAmountPage" - {
            "navigate to the GoodsToSellOrUseCYAController" in {
              val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "WorkplaceRunningCosts journey" - {
          "the page is MoreThan25HoursPage" - {
            "the user answers 'Yes'" - {
              "navigate to the WorkingFromHomeHoursController" in {
                val userAnswers    = emptyUserAnswers.set(MoreThan25HoursPage, MoreThan25Hours.Yes, Some(businessId)).success.value
                val expectedResult = workplaceRunningCosts.workingFromHome.routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(MoreThan25HoursPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "the user answers 'No'" - {
              "navigate to the WfhExpensesInfoController" in {
                val userAnswers    = emptyUserAnswers.set(MoreThan25HoursPage, MoreThan25Hours.No, Some(businessId)).success.value
                val expectedResult = workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId)

                navigator.nextPage(MoreThan25HoursPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is WorkingFromHomeHoursPage" - {
            "navigate to the WfhFlatRateOrActualCostsController" in {
              val expectedResult =
                workplaceRunningCosts.workingFromHome.routes.WfhFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(WorkingFromHomeHoursPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the page is WfhFlatRateOrActualCostsPage" - {
            "the answer is Flat Rate" - {
              "navigate to the DoYouLiveAtYourBusinessPremisesController" in {
                val userAnswers =
                  emptyUserAnswers.set(WfhFlatRateOrActualCostsPage, WfhFlatRateOrActualCosts.FlatRate, Some(businessId)).success.value
                val expectedResult =
                  workplaceRunningCosts.workingFromBusinessPremises.routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "the answer is Actual Costs" - {
              "navigate to the WfhExpensesInfoController" in {
                val userAnswers =
                  emptyUserAnswers.set(WfhFlatRateOrActualCostsPage, WfhFlatRateOrActualCosts.ActualCosts, Some(businessId)).success.value
                val expectedResult =
                  workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId)

                navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }

          "the page is BusinessPremisesAmountPage" - {
            "navigate to the BusinessPremisesAmountController" ignore {
              // TODO 6983 add full suite of tests
              val expectedResult =
                workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(BusinessPremisesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "AdvertisingOrMarketing journey" - {
          "the page is AdvertisingOrMarketingAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the AdvertisingOrMarketingDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("advertisingOrMarketing" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = advertisingOrMarketing.routes.AdvertisingDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(AdvertisingOrMarketingAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the AdvertisingCyaPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("advertisingOrMarketing" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(AdvertisingOrMarketingAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is AdvertisingOrMarketingDisallowableAmountPage" - {
            "navigate to the AdvertisingCyaPage" in {
              val expectedResult = advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(AdvertisingOrMarketingDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
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
        }

        "StaffCosts journey" - {
          "the page is StaffCostsAmountPage" - {
            "should navigate to the StaffCostsDisallowableAmountController" - {
              "when expenses are disallowable" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, DisallowableStaffCosts.Yes, Some(businessId)).success.value

                val expectedResult =
                  staffCosts.routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "should navigate to the StaffCostsCYAController" - {
              "when expenses are not disallowable" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, DisallowableStaffCosts.No, Some(businessId)).success.value

                val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is StaffCostsDisallowableAmountController" - {
            "navigate to the StaffCostsCYAController" in {
              val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(StaffCostsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "DisallowableSubcontractorCosts journey" - {
          "the page is ConstructionIndustryAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the ConstructionIndustryDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableSubcontractorCosts" -> "yes"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = construction.routes.ConstructionIndustryDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(ConstructionIndustryAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "navigate to the ConstructionIndustryCYAPage" - {
              "if all expenses were claimed as allowable" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableSubcontractorCosts" -> "no"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(ConstructionIndustryAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is ConstructionIndustryDisallowableAmountPage" - {
            "navigate to the ConstructionIndustryCYAPage" in {
              val expectedResult = construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(ConstructionIndustryDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "DisallowableProfessionalFees journey" - {
          "the page is ProfessionalFeesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the ProfessionalFeesDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableProfessionalFees" -> "yes"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = professionalFees.routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(ProfessionalFeesAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "navigate to the ProfessionalFeesCYAPage" - {
              "if all expenses were claimed as allowable" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableProfessionalFees" -> "no"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(ProfessionalFeesAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is ProfessionalFeesDisallowableAmountController" - {
            "navigate to the ProfessionalFeesCYAController" in {
              val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(ProfessionalFeesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "DisallowableInterest journey" - {
          "the page is InterestAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the InterestDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableInterest" -> "yes"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = interest.routes.InterestDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(InterestAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "navigate to the InterestCYAPage" - {
              "if all expenses were claimed as allowable" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableInterest" -> "no"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = interest.routes.InterestCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(InterestAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is InterestDisallowableAmountController" - {
            "navigate to the InterestCYAController" in {
              val expectedResult = interest.routes.InterestCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(InterestDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "IrrecoverableDebts journey" - {
          "the page is IrrecoverableDebtsAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the IrrecoverableDebtsDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableIrrecoverableDebts" -> "yes"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = irrecoverableDebts.routes.IrrecoverableDebtsDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(IrrecoverableDebtsAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "no disallowable expenses" - {
              "navigate to the IrrecoverableDebtsCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableIrrecoverableDebts" -> "no"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = irrecoverableDebts.routes.IrrecoverableDebtsCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(IrrecoverableDebtsDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is IrrecoverableDebtsDisallowableAmountPage" - {
            "navigate to the IrrecoverableDebtsCYAController" in {
              val expectedResult = irrecoverableDebts.routes.IrrecoverableDebtsCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(IrrecoverableDebtsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
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
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OfficeSuppliesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OfficeSuppliesDisallowableAmountPage" - {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is OtherExpensesAmountPage" - {
          "navigate to the OtherExpensesCYAController" in {
            val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OtherExpensesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OtherExpensesDisallowableAmountPage" - {
          "navigate to the OtherExpensesCYAController" in {
            val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is GoodsToSellOrUseAmountPage" - {
          "navigate to the GoodsToSellOrUseCYAController" in {
            val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(GoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is DisallowableGoodsToSellOrUseAmountPage" - {
          "navigate to the GoodsToSellOrUseCYAController" in {
            val expectedResult = GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is AdvertisingOrMarketingAmountPage" - {
          "navigate to the AdvertisingCyaPage" in {
            val expectedResult = advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(AdvertisingOrMarketingAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is AdvertisingOrMarketingDisallowableAmountPage" - {
          "navigate to the AdvertisingCyaPage" in {
            val expectedResult = advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(AdvertisingOrMarketingDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
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

        "the page is ConstructionIndustryAmountPage" - {
          "navigate to the ConstructionIndustryCYAPage" in {
            val expectedResult = construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ConstructionIndustryAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is ConstructionIndustryDisallowableAmountPage" - {
          "navigate to the ConstructionIndustryCYAPage" in {
            val expectedResult = construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ConstructionIndustryDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is ProfessionalFeesAmountPage" - {
          "navigate to the ProfessionalFeesCYAController" in {
            val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ProfessionalFeesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is ProfessionalFeesDisallowableAmountPage" - {
          "navigate to the ProfessionalFeesCYAController" in {
            val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ProfessionalFeesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is InterestAmountPage" - {
          "navigate to the InterestCYAController" in {
            val expectedResult = interest.routes.InterestCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(InterestAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is InterestDisallowableAmountPage" - {
          "navigate to the InterestCYAController" in {
            val expectedResult = interest.routes.InterestCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(InterestDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
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
