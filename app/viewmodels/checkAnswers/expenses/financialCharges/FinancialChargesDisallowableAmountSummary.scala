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

package viewmodels.checkAnswers.expenses.financialCharges

import controllers.journeys.expenses.financialCharges.routes._
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.DisallowableOtherFinancialCharges
import models.journeys.expenses.individualCategories.DisallowableOtherFinancialCharges._
import pages.expenses.financialCharges._
import pages.expenses.tailoring.individualCategories.DisallowableOtherFinancialChargesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.ImplicitConversions
import viewmodels.checkAnswers.buildRowBigDecimal

object FinancialChargesDisallowableAmountSummary extends ImplicitConversions {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableOtherFinancialChargesPage, Some(businessId))
      .filter(areFinancialChargesDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      amount             <- answers.get(FinancialChargesAmountPage, Some(businessId))
      disallowableAmount <- answers.get(FinancialChargesDisallowableAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"financialChargesDisallowableAmount.title.$userType", formatMoney(amount)),
      "financialChargesDisallowableAmount.change.hidden"
    )

  private def areFinancialChargesDisallowable(otherExpenses: DisallowableOtherFinancialCharges): Boolean =
    otherExpenses match {
      case Yes => true
      case No  => false
    }

}
