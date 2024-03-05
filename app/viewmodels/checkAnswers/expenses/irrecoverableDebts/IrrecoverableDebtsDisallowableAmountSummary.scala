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

package viewmodels.checkAnswers.expenses.irrecoverableDebts

import controllers.journeys.expenses.irrecoverableDebts.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.irrecoverableDebts._
import pages.expenses.tailoring.individualCategories.DisallowableIrrecoverableDebtsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object IrrecoverableDebtsDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableIrrecoverableDebtsPage, Some(businessId))
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(IrrecoverableDebtsDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(IrrecoverableDebtsAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.IrrecoverableDebtsDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"irrecoverableDebtsDisallowableAmount.title.$userType", allowableAmount),
      "irrecoverableDebtsDisallowableAmount.change.hidden"
    )

}
