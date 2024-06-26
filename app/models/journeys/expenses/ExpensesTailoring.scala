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

package models.journeys.expenses

import models.common.{Enumerable, UserType, WithName}
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, TaxiMinicabOrRoadHaulagePage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories._
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ExpensesTailoring

object ExpensesTailoring extends Enumerable.Implicits {

  case object TotalAmount          extends WithName("totalAmount") with ExpensesTailoring
  case object IndividualCategories extends WithName("individualCategories") with ExpensesTailoring
  case object NoExpenses           extends WithName("noExpenses") with ExpensesTailoring

  val values: Seq[ExpensesTailoring] = Seq(
    TotalAmount,
    IndividualCategories,
    NoExpenses
  )

  val tailoringList = List(
    AdvertisingOrMarketingAmountPage,
    AdvertisingOrMarketingDisallowableAmountPage,
    AdvertisingOrMarketingPage,
    BusinessPremisesAmountPage,
    BusinessPremisesDisallowableAmountPage,
    ConstructionIndustryAmountPage,
    ConstructionIndustryDisallowableAmountPage,
    DepreciationDisallowableAmountPage,
    DepreciationPage,
    DisallowableGoodsToSellOrUseAmountPage,
    DisallowableInterestPage,
    DisallowableIrrecoverableDebtsPage,
    DisallowableOtherFinancialChargesPage,
    DisallowableProfessionalFeesPage,
    DisallowableStaffCostsPage,
    DisallowableSubcontractorCostsPage,
    EntertainmentAmountPage,
    EntertainmentCostsPage,
    FinancialChargesAmountPage,
    FinancialChargesDisallowableAmountPage,
    FinancialExpensesPage,
    GoodsToSellOrUseAmountPage,
    GoodsToSellOrUsePage,
    InterestAmountPage,
    InterestDisallowableAmountPage,
    IrrecoverableDebtsAmountPage,
    IrrecoverableDebtsDisallowableAmountPage,
    LiveAtBusinessPremisesPage,
    LivingAtBusinessPremisesOnePerson,
    LivingAtBusinessPremisesThreePlusPeople,
    LivingAtBusinessPremisesTwoPeople,
    MoreThan25HoursPage,
    OfficeSuppliesAmountPage,
    OfficeSuppliesDisallowableAmountPage,
    OfficeSuppliesPage,
    OtherExpensesAmountPage,
    OtherExpensesDisallowableAmountPage,
    OtherExpensesPage,
    ProfessionalFeesAmountPage,
    ProfessionalFeesDisallowableAmountPage,
    ProfessionalServiceExpensesPage,
    RepairsAndMaintenanceAmountPage,
    RepairsAndMaintenanceDisallowableAmountPage,
    RepairsAndMaintenancePage,
    StaffCostsAmountPage,
    StaffCostsDisallowableAmountPage,
    TaxiMinicabOrRoadHaulagePage,
    TotalExpensesPage,
    TravelForWorkPage,
    WfbpClaimingAmountPage,
    WfbpFlatRateOrActualCostsPage,
    WfhClaimingAmountPage,
    WfhFlatRateOrActualCostsPage,
    WorkFromBusinessPremisesPage,
    WorkFromHomePage,
    WorkingFromHomeHours25To50,
    WorkingFromHomeHours51To100,
    WorkingFromHomeHours101Plus
  )

  def options(userType: UserType)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    val optUserType = if (value == NoExpenses) s".$userType" else ""
    RadioItem(
      content = Text(messages(s"expenses.$value$optUserType")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[ExpensesTailoring] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
