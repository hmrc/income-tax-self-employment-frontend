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

import models.journeys.expenses.individualCategories._
import play.api.libs.json.{Format, Json}

case class ExpensesTailoringIndividualCategoriesAnswers(
    expensesCategories: ExpensesTailoring,
    officeSupplies: OfficeSupplies,
    taxiMinicabOrRoadHaulage: TaxiMinicabOrRoadHaulage,
    goodsToSellOrUse: GoodsToSellOrUse,
    repairsAndMaintenance: RepairsAndMaintenance,
    workFromHome: WorkFromHome,
    workFromBusinessPremises: WorkFromBusinessPremises,
    travelForWork: TravelForWork,
    advertisingOrMarketing: AdvertisingOrMarketing,
    entertainmentCosts: Option[EntertainmentCosts],
    professionalServiceExpenses: List[ProfessionalServiceExpenses],
    financialExpenses: List[FinancialExpenses],
    depreciation: Depreciation,
    otherExpenses: OtherExpenses,
    disallowableInterest: Option[DisallowableInterest],
    disallowableOtherFinancialCharges: Option[DisallowableOtherFinancialCharges],
    disallowableIrrecoverableDebts: Option[DisallowableIrrecoverableDebts],
    disallowableStaffCosts: Option[DisallowableStaffCosts],
    disallowableSubcontractorCosts: Option[DisallowableSubcontractorCosts],
    disallowableProfessionalFees: Option[DisallowableProfessionalFees]
)

object ExpensesTailoringIndividualCategoriesAnswers {
  implicit val format: Format[ExpensesTailoringIndividualCategoriesAnswers] = Json.format[ExpensesTailoringIndividualCategoriesAnswers]
}