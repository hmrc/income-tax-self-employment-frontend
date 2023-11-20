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
import controllers.journeys.expenses.tailoring.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.journeys.Journey.ExpensesTailoring
import models.journeys.expenses.FinancialExpenses._
import models.journeys.expenses.ProfessionalServiceExpenses._
import models.journeys.expenses.{FinancialExpenses, ProfessionalServiceExpenses}
import pages._
import pages.expenses.tailoring._

class ExpensesTailoringNavigatorSpec extends SpecBase {

  val navigator = new ExpensesTailoringNavigator

  case object UnknownPage extends Page

  "ExpensesTailoringNavigator" - {

    "in Normal mode" - {

      "OfficeSuppliesPage must go to the TaxiMinicabOrRoadHaulagePage" in {

        navigator.nextPage(OfficeSuppliesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "TaxiMinicabOrRoadHaulagePage must go to the GoodsToSellOrUsePage" in {

        navigator.nextPage(TaxiMinicabOrRoadHaulagePage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          GoodsToSellOrUseController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "GoodsToSellOrUsePage must go to the RepairsAndMaintenancePage" in {

        navigator.nextPage(GoodsToSellOrUsePage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          RepairsAndMaintenanceController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "RepairsAndMaintenancePage must go to the WorkFromHomePage" in {

        navigator.nextPage(RepairsAndMaintenancePage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          WorkFromHomeController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "WorkFromHomePage must go to the WorkFromBusinessPremisesPage" in {

        navigator.nextPage(WorkFromHomePage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          WorkFromBusinessPremisesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "WorkFromBusinessPremisesPage must go to the TravelForWorkPage" in {

        navigator.nextPage(WorkFromBusinessPremisesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          TravelForWorkController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "TravelForWorkPage must go to the AdvertisingOrMarketingPage" in {

        navigator.nextPage(TravelForWorkPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          AdvertisingOrMarketingController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "AdvertisingOrMarketingPage must go to the" - {
        "EntertainmentCostsPage when accounting type is 'ACCRUAL'" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId, Some(true)) mustBe
            EntertainmentCostsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "ProfessionalServiceExpensesPage when accounting type is 'CASH'" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId, Some(false)) mustBe
            ProfessionalServiceExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there is no accounting type" in {

          navigator.nextPage(AdvertisingOrMarketingPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "EntertainmentCostsPage must go to the ProfessionalServiceExpensesPage" in {

        navigator.nextPage(EntertainmentCostsPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          ProfessionalServiceExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "ProfessionalServiceExpensesPage must go to the" - {
        "DisallowableStaffCostsPage when 'Staff' checkbox is checked" in {

          val userAnswers = emptyUserAnswers
            .set[Set[ProfessionalServiceExpenses]](
              ProfessionalServiceExpensesPage,
              Set(Staff): Set[ProfessionalServiceExpenses],
              Some(stubbedBusinessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableStaffCostsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableSubcontractorCostsPage when 'Construction' checkbox is checked but not 'Staff'" in {

          val userAnswers = emptyUserAnswers
            .set(ProfessionalServiceExpensesPage, Set(Construction): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Staff' or 'Construction'" in {

          val userAnswers = emptyUserAnswers
            .set(ProfessionalServiceExpensesPage, Set(ProfessionalFees): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId))
            .success
            .value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(ProfessionalServiceExpensesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableStaffCostsPage must go to the" - {
        "DisallowableSubcontractorCostsPage when 'Construction' checkbox is checked" in {

          val userAnswers = emptyUserAnswers
            .set(ProfessionalServiceExpensesPage, Set(Construction): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId))
            .success
            .value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableSubcontractorCostsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Construction'" in {

          val userAnswers = emptyUserAnswers
            .set(ProfessionalServiceExpensesPage, Set(ProfessionalFees): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId))
            .success
            .value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableStaffCostsPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableSubcontractorCostsPage must go to the" - {
        "DisallowableProfessionalFeesPage when 'ProfessionalFees' checkbox is checked but not 'Construction'" in {

          val userAnswers =
            emptyUserAnswers
              .set(ProfessionalServiceExpensesPage, Set(ProfessionalFees): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId))
              .success
              .value

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableProfessionalFeesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "FinancialExpensesPage when 'No profession services' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(ProfessionalServiceExpensesPage, Set(No): Set[ProfessionalServiceExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            FinancialExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableSubcontractorCostsPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableProfessionalFeesPage must go to the FinancialExpensesPage" in {

        navigator.nextPage(DisallowableProfessionalFeesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          FinancialExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "FinancialExpensesPage must go to the" - {
        "DisallowableInterestPage when 'Interest' checkbox is checked" in {

          val userAnswers = emptyUserAnswers.set(FinancialExpensesPage, Set(Interest): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableInterestController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' checkbox is checked but not 'Interest'" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(OtherFinancialCharges): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked but not 'Interest' or 'OtherFinancialCharges'" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(FinancialExpensesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DepreciationController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(FinancialExpensesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableInterestPage must go to the" - {
        "DisallowableOtherFinancialChargesPage when 'OtherFinancialCharges' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(OtherFinancialCharges): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableOtherFinancialChargesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked but not 'OtherFinancialCharges'" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableInterestPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DepreciationController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableInterestPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableOtherFinancialChargesPage must go to the" - {
        "DisallowableIrrecoverableDebtsPage when 'IrrecoverableDebts' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(IrrecoverableDebts): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "DepreciationPage when 'NoFinancialExpenses' checkbox is checked" in {

          val userAnswers =
            emptyUserAnswers.set(FinancialExpensesPage, Set(NoFinancialExpenses): Set[FinancialExpenses], Some(stubbedBusinessId)).success.value

          navigator.nextPage(DisallowableOtherFinancialChargesPage, NormalMode, userAnswers, taxYear, stubbedBusinessId) mustBe
            DepreciationController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(DisallowableInterestPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "DisallowableIrrecoverableDebtsPage must go to the DepreciationPage" in {

        navigator.nextPage(DisallowableIrrecoverableDebtsPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          DepreciationController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "OtherExpensesPage must go to the Expenses CYA page" ignore { // TODO unignore when CYA page is created

        navigator.nextPage(OtherExpensesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          OtherExpensesController.onPageLoad(taxYear, stubbedBusinessId, NormalMode)
      }

      "Expenses CYA page must go to the Section Completed page with Income journey" ignore { // TODO unignore when CYA page is created

        navigator.nextPage(OtherExpensesPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe
          SectionCompletedStateController.onPageLoad(taxYear, stubbedBusinessId, ExpensesTailoring.toString, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers, taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }
  }

}
