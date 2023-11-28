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

import controllers.journeys.expenses.tailoring.routes._
import controllers.standard.routes._
import models._
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.FinancialExpenses.{Interest, IrrecoverableDebts, NoFinancialExpenses, OtherFinancialCharges}
import models.journeys.expenses.ProfessionalServiceExpenses.{Construction, No, ProfessionalFees, Staff}
import pages._
import pages.expenses.tailoring._
import play.api.mvc.Call
import controllers.journeys.expenses.tailoring
import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesTailoringNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => (TaxYear, String, Option[Boolean]) => Call = {

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
            case Some(false) => ProfessionalServiceExpensesController.onPageLoad(taxYear, BusinessId(businessId), NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case EntertainmentCostsPage =>
      _ => (taxYear, businessId, _) => ProfessionalServiceExpensesController.onPageLoad(taxYear, BusinessId(businessId), NormalMode)

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

    case OtherExpensesPage => _ => (taxYear, businessId, _) => tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, BusinessId(businessId))

    case _ => _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, String, Option[Boolean]) => Call = { case _ =>
    _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, isAccrual: Option[Boolean] = None): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId.value, isAccrual)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId.value, isAccrual)
    }

  /** User also for CYA pages
    */
  def nextNormalRoute(sourcePage: Page,
                      userAnswers: UserAnswers,
                      taxYear: TaxYear,
                      businessId: BusinessId,
                      accountingType: Option[AccountingType]): Call =
    normalRoutes(sourcePage)(userAnswers)(taxYear, businessId.value, accountingType.map(_ == Accrual))
}
