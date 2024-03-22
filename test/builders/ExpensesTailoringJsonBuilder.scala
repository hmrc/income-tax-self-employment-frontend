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

import pages.expenses.tailoring.individualCategories.{
  DepreciationPage,
  DisallowableIrrecoverableDebtsPage,
  DisallowableProfessionalFeesPage,
  EntertainmentCostsPage,
  WorkFromHomePage
}
import play.api.libs.json.Json

object ExpensesTailoringJsonBuilder {

  val allNoIndividualCategoriesAnswers = Json.obj(
    "expensesCategories"            -> "individualCategories",
    "officeSupplies"                -> "no",
    "officeSupplies"                -> "no",
    "goodsToSellOrUse"              -> "no",
    "repairsAndMaintenance"         -> "no",
    WorkFromHomePage.toString       -> false,
    "workFromBusinessPremises"      -> "no",
    "travelForWork"                 -> "no",
    "advertisingOrMarketing"        -> "no",
    EntertainmentCostsPage.toString -> false,
    "professionalServiceExpenses"   -> Set("no"),
    "financialExpenses"             -> Set("noFinancialExpenses"),
    DepreciationPage.toString       -> false,
    "otherExpenses"                 -> "no"
  )

  val allYesIndividualCategoriesAnswers = Json.obj(
    "expensesCategories"                        -> "individualCategories",
    "officeSupplies"                            -> "yesAllowable",
    "goodsToSellOrUse"                          -> "yesDisallowable",
    "repairsAndMaintenance"                     -> "yesDisallowable",
    WorkFromHomePage.toString                   -> true,
    "workFromBusinessPremises"                  -> "yesAllowable",
    "travelForWork"                             -> "yesDisallowable",
    "advertisingOrMarketing"                    -> "yesDisallowable",
    EntertainmentCostsPage.toString             -> true,
    "professionalServiceExpenses"               -> Set("staff", "construction", "professionalFees"),
    "disallowableStaffCosts"                    -> "yes",
    "disallowableSubcontractorCosts"            -> "yes",
    DisallowableProfessionalFeesPage.toString   -> true,
    "financialExpenses"                         -> Set("interest", "otherFinancialCharges", "irrecoverableDebts"),
    "disallowableInterest"                      -> "yes",
    "disallowableOtherFinancialCharges"         -> "yes",
    DisallowableIrrecoverableDebtsPage.toString -> true,
    DepreciationPage.toString                   -> true,
    "otherExpenses"                             -> "yesAllowable"
  )

  val mixedIndividualCategoriesAnswers = Json.obj(
    "expensesCategories"                      -> "individualCategories",
    "officeSupplies"                          -> "yesAllowable",
    "goodsToSellOrUse"                        -> "no",
    "repairsAndMaintenance"                   -> "yesDisallowable",
    WorkFromHomePage.toString                 -> true,
    "workFromBusinessPremises"                -> "yesAllowable",
    "travelForWork"                           -> "yesDisallowable",
    "advertisingOrMarketing"                  -> "no",
    EntertainmentCostsPage.toString           -> false,
    "professionalServiceExpenses"             -> Set("staff", "professionalFees"),
    "disallowableStaffCosts"                  -> "yes",
    DisallowableProfessionalFeesPage.toString -> false,
    "financialExpenses"                       -> Set("noFinancialExpenses"),
    DepreciationPage.toString                 -> false,
    "otherExpenses"                           -> "yesAllowable"
  )

  val noExpensesAnswers = Json.obj(
    "expensesCategories" -> "noExpenses"
  )

  val totalAmountAnswers = Json.obj(
    "expensesCategories" -> "noExpenses",
    "totalAmount"        -> "50000"
  )

}
