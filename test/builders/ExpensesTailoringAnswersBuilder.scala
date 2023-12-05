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

package builders

import models.journeys.expenses._

object ExpensesTailoringAnswersBuilder {

  val expensesTailoringAnswers: ExpensesTailoringAnswers = ExpensesTailoringAnswers(
    ExpensesTailoring.IndividualCategories,
    OfficeSupplies.YesDisallowable,
    TaxiMinicabOrRoadHaulage.No,
    GoodsToSellOrUse.No,
    RepairsAndMaintenance.YesDisallowable,
    WorkFromHome.Yes,
    WorkFromBusinessPremises.YesDisallowable,
    TravelForWork.No,
    AdvertisingOrMarketing.No,
    None,
    List(ProfessionalServiceExpenses.No),
    List(FinancialExpenses.Interest, FinancialExpenses.OtherFinancialCharges),
    Depreciation.No,
    OtherExpenses.YesDisallowable,
    Some(DisallowableInterest.Yes),
    Some(DisallowableOtherFinancialCharges.No),
    None,
    None,
    None,
    None
  )
}
