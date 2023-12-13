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
import controllers.journeys.expenses.tailoring
import controllers.journeys.expenses.tailoring.individualCategories.routes._
import controllers.journeys.expenses.tailoring.simplifiedExpenses
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.journeys.Journey.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.journeys.expenses.individualCategories.FinancialExpenses.{Interest, IrrecoverableDebts, NoFinancialExpenses, OtherFinancialCharges}
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses.{Construction, No, ProfessionalFees, Staff}
import models.journeys.expenses.individualCategories.{FinancialExpenses, ProfessionalServiceExpenses}
import pages._
import pages.expenses.tailoring._
import pages.expenses.tailoring.individualCategories._
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage

class ExpensesTailoringNavigatorSpec extends SpecBase {

  val navigator = new ExpensesTailoringNavigator

  case object UnknownPage extends Page

  "ExpensesTailoringNavigator" - {

    "in Normal mode" - {

      "ExpensesCategoriesPage must go to the" - {
        "TotalExpensesPage when 'TotalAmount' is selected" in {

          val userAnswers =
            emptyUserAnswers.set(ExpensesCategoriesPage, TotalAmount, Some(businessId)).success.value

          navigator.nextPage(ExpensesCategoriesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            simplifiedExpenses.routes.TotalExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "OfficeSuppliesPage when 'IndividualCategories' is selected" in {

          val userAnswers =
            emptyUserAnswers.set(ExpensesCategoriesPage, IndividualCategories, Some(businessId)).success.value

          navigator.nextPage(ExpensesCategoriesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            OfficeSuppliesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "ExpensesTailoringCYAPage when 'NoExpenses' is selected" in {

          val userAnswers =
            emptyUserAnswers.set(ExpensesCategoriesPage, NoExpenses, Some(businessId)).success.value

          navigator.nextPage(ExpensesCategoriesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(ExpensesCategoriesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "TotalExpensesPage must go to the TaxiMinicabOrRoadHaulagePage" in {

        navigator.nextPage(TotalExpensesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
      }

      "OfficeSuppliesPage must go to the TaxiMinicabOrRoadHaulagePage" in {

        navigator.nextPage(OfficeSuppliesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "TaxiMinicabOrRoadHaulagePage must go to the GoodsToSellOrUsePage" in {

        navigator.nextPage(TaxiMinicabOrRoadHaulagePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "GoodsToSellOrUsePage must go to the RepairsAndMaintenancePage" in {

        navigator.nextPage(GoodsToSellOrUsePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          RepairsAndMaintenanceController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "RepairsAndMaintenancePage must go to the WorkFromHomePage" in {

        navigator.nextPage(RepairsAndMaintenancePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          WorkFromHomeController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "WorkFromHomePage must go to the WorkFromBusinessPremisesPage" in {

        navigator.nextPage(WorkFromHomePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          WorkFromBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "WorkFromBusinessPremisesPage must go to the TravelForWorkPage" in {

        navigator.nextPage(WorkFromBusinessPremisesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          TravelForWorkController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "TravelForWorkPage must go to the AdvertisingOrMarketingPage" in {

        navigator.nextPage(TravelForWorkPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          AdvertisingOrMarketingController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "AdvertisingOrMarketingPage must go to the" - {
        "EntertainmentCostsPage when accounting type is 'ACCRUAL'" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, businessId, Some(true)) mustBe
            EntertainmentCostsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "ProfessionalServiceExpensesPage when accounting type is 'CASH'" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, businessId, Some(false)) mustBe
            ProfessionalServiceExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there is no accounting type" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "EntertainmentCostsPage must go to the ProfessionalServiceExpensesPage" in {

        navigator.nextPage(EntertainmentCostsPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          ProfessionalServiceExpensesController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "ProfessionalServiceExpensesPage must go to the" - {
        "DisallowableStaffCostsPage when 'Staff' checkbox is checked" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(Staff): Set[ProfessionalServiceExpenses], Some(businessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableStaffCostsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableSubcontractorCostsPage when 'Construction' checkbox is checked but not 'Staff'" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](
              ProfessionalServiceExpensesPage,
              Set(Construction): Set[ProfessionalServiceExpenses],
              Some(businessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Staff' or 'Construction'" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](
              ProfessionalServiceExpensesPage,
              Set(ProfessionalFees): Set[ProfessionalServiceExpenses],
              Some(businessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableStaffCostsPage must go to the" - {
        "DisallowableSubcontractorCostsPage when 'Construction' checkbox is checked" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](
              ProfessionalServiceExpensesPage,
              Set(Construction): Set[ProfessionalServiceExpenses],
              Some(businessId))
            .success
            .value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Construction'" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](
              ProfessionalServiceExpensesPage,
              Set(ProfessionalFees): Set[ProfessionalServiceExpenses],
              Some(businessId))
            .success
            .value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableSubcontractorCostsPage must go to the" - {
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Construction'" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[ProfessionalServiceExpenses]](
                ProfessionalServiceExpensesPage,
                Set(ProfessionalFees): Set[ProfessionalServiceExpenses],
                Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableProfessionalFeesPage must go to the FinancialExpensesPage" in {

        navigator.nextPage(DisallowableProfessionalFeesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "FinancialExpensesPage must go to the" - {
        "DisallowableInterestPage when 'Interest' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(Interest): Set[FinancialExpenses], Some(businessId)).success.value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableInterestController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' checkbox is checked but not 'Interest'" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(OtherFinancialCharges): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked but not 'Interest' or 'OtherFinancialCharges'" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(FinancialExpensesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableInterestPage must go to the" - {
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(OtherFinancialCharges): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked but not 'OtherFinancialCharges'" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableInterestPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableOtherFinancialChargesPage must go to the" - {
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers
              .set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(businessId))
              .success
              .value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableInterestPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableIrrecoverableDebtsPage must go to the DepreciationPage" in {

        navigator.nextPage(DisallowableIrrecoverableDebtsPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "OtherExpensesPage must go to the Expenses CYA page" in {

        navigator.nextPage(OtherExpensesPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
      }

      "Expenses CYA page must go to the Section Completed page with Income journey" in {

        navigator.nextPage(ExpensesTailoringCYAPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
          SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesTailoring.toString, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "ExpensesCategoriesPage must go to the ExpensesTailoringCYAPage" in {

        navigator.nextPage(ExpensesCategoriesPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
          tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
      }

      "ProfessionalServiceExpensesPage must go to the" - {
        "DisallowableStaffCostsPage when 'Staff' is checked and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(Staff), Some(businessId)).success.value

          navigator.nextPage(ProfessionalServiceExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableStaffCostsController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableSubcontractorCostsPage when 'Construction' but not 'Staff' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(Construction), Some(businessId)).success.value

          navigator.nextPage(ProfessionalServiceExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' but not 'Construction' and/or 'Staff' is checked, and that page's data is empty" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(ProfessionalFees), Some(businessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No), Some(businessId)).success.value

          navigator.nextPage(ProfessionalServiceExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(ProfessionalServiceExpensesPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableStaffCostsPage must go to the" - {
        "DisallowableSubcontractorCostsPage when 'Construction' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(Construction), Some(businessId)).success.value

          navigator.nextPage(DisallowableStaffCostsPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' but not 'Construction' and/or 'Staff' is checked, and that page's data is empty" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(ProfessionalFees), Some(businessId))
            .success
            .value

          navigator.nextPage(DisallowableStaffCostsPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No), Some(businessId)).success.value

          navigator.nextPage(DisallowableStaffCostsPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableStaffCostsPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableSubcontractorCostsPage must go to the" - {
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' is checked, and that page's data is empty" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(ProfessionalFees), Some(businessId))
            .success
            .value

          navigator.nextPage(DisallowableSubcontractorCostsPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[ProfessionalServiceExpenses]](ProfessionalServiceExpensesPage, Set(No), Some(businessId)).success.value

          navigator.nextPage(DisallowableSubcontractorCostsPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableSubcontractorCostsPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "FinancialExpensesPage must go to the" - {
        "DisallowableInterestPage when 'Interest' is checked and that page's data is empty" in {

          val userAnswers = emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(Interest), Some(businessId)).success.value

          navigator.nextPage(FinancialExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableInterestController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' but not 'Interest' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(OtherFinancialCharges), Some(businessId)).success.value

          navigator.nextPage(FinancialExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' but not 'OtherFinancialCharges' and/or 'Interest' " +
          "is checked, and that page's data is empty" in {

            val userAnswers =
              emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts), Some(businessId)).success.value

            navigator.nextPage(FinancialExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
          }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses), Some(businessId)).success.value

          navigator.nextPage(FinancialExpensesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(FinancialExpensesPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableInterestPage must go to the" - {
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(OtherFinancialCharges), Some(businessId)).success.value

          navigator.nextPage(DisallowableInterestPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' but not 'OtherFinancialCharges' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts), Some(businessId)).success.value

          navigator.nextPage(DisallowableInterestPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses), Some(businessId)).success.value

          navigator.nextPage(DisallowableInterestPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableInterestPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableOtherFinancialChargesPage must go to the" - {
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' is checked, and that page's data is empty" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(IrrecoverableDebts), Some(businessId)).success.value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "ExpensesTailoringCYAPage when all checked answers have page data" in {

          val userAnswers =
            emptyUserAnswers.set[Set[FinancialExpenses]](FinancialExpensesPage, Set(NoFinancialExpenses), Some(businessId)).success.value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableOtherFinancialChargesPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from any other Expenses Tailoring page to the 'Check your details' page" in {

        navigator.nextPage(OtherExpensesPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
          tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
      }
    }
  }

}
