/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses

import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.FinancialExpenses.NoFinancialExpenses
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses.No
import models.journeys.expenses.individualCategories.{FinancialExpenses, ProfessionalServiceExpenses}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.tailoring.individualCategories._
import viewmodels.checkAnswers.expenses.tailoring.simplifiedExpenses.TotalExpensesSummary
import viewmodels.journeys.SummaryListCYA

package object tailoring {

  def buildTailoringSummaryList(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = {
    implicit val impUserAnswers: UserAnswers = answers
    implicit val impTaxYear: TaxYear         = taxYear
    implicit val impBusinessId: BusinessId   = businessId
    implicit val impUserType: UserType       = userType

    SummaryListCYA.summaryListOpt(
      List(
        ExpensesTailoringSummary.row(),
        TotalExpensesSummary.row(),
        OfficeSuppliesSummary.row(),
        TaxiMinicabOrRoadHaulageSummary.row(),
        GoodsToSellOrUseSummary.row(),
        RepairsAndMaintenanceSummary.row(),
        WorkFromHomeSummary.row(),
        WorkFromBusinessPremisesSummary.row(),
        TravelForWorkSummary.row(),
        AdvertisingOrMarketingSummary.row(),
        EntertainmentCostsSummary.row(),
        ProfessionalServiceExpensesSummary.row(),
        DisallowableStaffCostsSummary.row(),
        DisallowableSubcontractorCostsSummary.row(),
        DisallowableProfessionalFeesSummary.row(),
        FinancialExpensesSummary.row(),
        DisallowableInterestSummary.row(),
        DisallowableOtherFinancialChargesSummary.row(),
        DisallowableIrrecoverableDebtsSummary.row(),
        DepreciationSummary.row(),
        OtherExpensesSummary.row()
      )
    )
  }

  def formatAnswer(answer: String)(implicit messages: Messages): String =
    answer match {
      case "no"  => messages("site.no")
      case "yes" => messages("site.yes")
      case value => messages(s"expenses.$value.cya")
    }

  def formatProfessionalServiceExpensesAnswers(answers: Set[ProfessionalServiceExpenses], userType: UserType)(implicit messages: Messages): String =
    if (answers.contains(No)) {
      messages(s"professionalServiceExpenses.no.$userType")
    } else {
      answers.map(a => messages(s"professionalServiceExpenses.${a.toString}")).mkString("<br>")
    }

  def formatFinancialExpensesAnswers(answers: Set[FinancialExpenses], userType: UserType)(implicit messages: Messages): String =
    if (answers.contains(NoFinancialExpenses)) {
      messages(s"financialExpenses.noFinancialExpenses.$userType")
    } else {
      answers.map(a => messages(s"financialExpenses.${a.toString}.cya")).mkString("<br>")
    }
}
