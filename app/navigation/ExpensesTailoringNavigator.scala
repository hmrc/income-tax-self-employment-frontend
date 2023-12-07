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

import controllers.journeys.expenses.{simplifiedExpenses, tailoring}
import controllers.journeys.expenses.tailoring.routes._
import controllers.{journeys, standard}
import controllers.standard.routes._
import models.common.AccountingType.Accrual
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring._
import models.journeys.expenses.FinancialExpenses.{Interest, IrrecoverableDebts, NoFinancialExpenses, OtherFinancialCharges}
import models.journeys.expenses.ProfessionalServiceExpenses.{Construction, No, ProfessionalFees, Staff}
import models.{NormalMode, _}
import pages._
import pages.expenses.tailoring._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesTailoringNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId, Option[Boolean]) => Call = {

    case ExpensesCategoriesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ExpensesCategoriesPage, Some(businessId)) match {
            case Some(TotalAmount)          => simplifiedExpenses.routes.TotalExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(IndividualCategories) => tailoring.routes.OfficeSuppliesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(NoExpenses)           => tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _                          => standard.routes.JourneyRecoveryController.onPageLoad()
          }

    case OfficeSuppliesPage => _ => (taxYear, businessId, _) => TaxiMinicabOrRoadHaulageController.onPageLoad(taxYear, businessId, NormalMode)

    case TaxiMinicabOrRoadHaulagePage => _ => (taxYear, businessId, _) => GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)

    case GoodsToSellOrUsePage => _ => (taxYear, businessId, _) => RepairsAndMaintenanceController.onPageLoad(taxYear, businessId, NormalMode)

    case RepairsAndMaintenancePage => _ => (taxYear, businessId, _) => WorkFromHomeController.onPageLoad(taxYear, businessId, NormalMode)

    case WorkFromHomePage => _ => (taxYear, businessId, _) => WorkFromBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)

    case WorkFromBusinessPremisesPage => _ => (taxYear, businessId, _) => TravelForWorkController.onPageLoad(taxYear, businessId, NormalMode)

    case TravelForWorkPage => _ => (taxYear, businessId, _) => AdvertisingOrMarketingController.onPageLoad(taxYear, businessId, NormalMode)

    case AdvertisingOrMarketingPage =>
      _ =>
        (taxYear, businessId, optIsAccrual) =>
          optIsAccrual match {
            case Some(true)  => EntertainmentCostsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => ProfessionalServiceExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case EntertainmentCostsPage => _ => (taxYear, businessId, _) => ProfessionalServiceExpensesController.onPageLoad(taxYear, businessId, NormalMode)

    case ProfessionalServiceExpensesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(No)    => FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(Staff) => DisallowableStaffCostsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(Construction) =>
              DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(ProfessionalFees) =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
            case _ => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableStaffCostsPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(Construction) =>
              DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(ProfessionalFees) =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(_) => FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableSubcontractorCostsPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(ProfessionalFees) =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(_) => FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableProfessionalFeesPage => _ => (taxYear, businessId, _) => FinancialExpensesController.onPageLoad(taxYear, businessId, NormalMode)

    case FinancialExpensesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(NoFinancialExpenses) => DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(Interest)            => DisallowableInterestController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(OtherFinancialCharges) =>
              DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(IrrecoverableDebts) =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
            case _ => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableInterestPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(OtherFinancialCharges) =>
              DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(seq) if seq.contains(IrrecoverableDebts) =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(_) => DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableOtherFinancialChargesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq) if seq.contains(IrrecoverableDebts) =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(_) => DepreciationController.onPageLoad(taxYear, businessId, NormalMode)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableIrrecoverableDebtsPage => _ => (taxYear, businessId, _) => DepreciationController.onPageLoad(taxYear, businessId, NormalMode)

    case DepreciationPage => _ => (taxYear, businessId, _) => OtherExpensesController.onPageLoad(taxYear, businessId, NormalMode)

    case OtherExpensesPage => _ => (taxYear, businessId, _) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)

    case ExpensesTailoringCYAPage =>
      _ =>
        (taxYear, businessId, _) =>
          journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesTailoring.toString, NormalMode)

    case _ => _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, BusinessId, Option[Boolean]) => Call = {

    case ExpensesCategoriesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ExpensesCategoriesPage, Some(businessId)) match {
            case Some(TotalAmount)          => simplifiedExpenses.routes.TotalExpensesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(IndividualCategories) => tailoring.routes.OfficeSuppliesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(NoExpenses)           => tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _                          => standard.routes.JourneyRecoveryController.onPageLoad()
          }

    case ProfessionalServiceExpensesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(Staff) &&
                  userAnswers.get(DisallowableStaffCostsPage, Some(businessId)).isEmpty =>
              DisallowableStaffCostsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(Construction) &&
                  userAnswers.get(DisallowableSubcontractorCostsPage, Some(businessId)).isEmpty =>
              DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(ProfessionalFees) &&
                  userAnswers.get(DisallowableProfessionalFeesPage, Some(businessId)).isEmpty =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableStaffCostsPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(Construction) &&
                  userAnswers.get(DisallowableSubcontractorCostsPage, Some(businessId)).isEmpty =>
              DisallowableSubcontractorCostsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(ProfessionalFees) &&
                  userAnswers.get(DisallowableProfessionalFeesPage, Some(businessId)).isEmpty =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableSubcontractorCostsPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(ProfessionalFees) &&
                  userAnswers.get(DisallowableProfessionalFeesPage, Some(businessId)).isEmpty =>
              DisallowableProfessionalFeesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case FinancialExpensesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(Interest) &&
                  userAnswers.get(DisallowableInterestPage, Some(businessId)).isEmpty =>
              DisallowableInterestController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(OtherFinancialCharges) &&
                  userAnswers.get(DisallowableOtherFinancialChargesPage, Some(businessId)).isEmpty =>
              DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(IrrecoverableDebts) &&
                  userAnswers.get(DisallowableIrrecoverableDebtsPage, Some(businessId)).isEmpty =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableInterestPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(OtherFinancialCharges) &&
                  userAnswers.get(DisallowableOtherFinancialChargesPage, Some(businessId)).isEmpty =>
              DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(seq)
                if seq.contains(IrrecoverableDebts) &&
                  userAnswers.get(DisallowableIrrecoverableDebtsPage, Some(businessId)).isEmpty =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case DisallowableOtherFinancialChargesPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(FinancialExpensesPage, Some(businessId)) match {
            case Some(seq)
                if seq.contains(IrrecoverableDebts) &&
                  userAnswers.get(DisallowableIrrecoverableDebtsPage, Some(businessId)).isEmpty =>
              DisallowableIrrecoverableDebtsController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(_) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
            case _       => JourneyRecoveryController.onPageLoad()
          }

    case _ => _ => (taxYear, businessId, _) => ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, isAccrual: Option[Boolean] = None): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId, isAccrual)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId, isAccrual)
    }

  /** The "normal" route is also used for CYA pages */
  def nextNormalRoute(sourcePage: Page,
                      userAnswers: UserAnswers,
                      taxYear: TaxYear,
                      businessId: BusinessId,
                      accountingType: Option[AccountingType]): Call =
    normalRoutes(sourcePage)(userAnswers)(taxYear, businessId, accountingType.map(_ == Accrual))
}
