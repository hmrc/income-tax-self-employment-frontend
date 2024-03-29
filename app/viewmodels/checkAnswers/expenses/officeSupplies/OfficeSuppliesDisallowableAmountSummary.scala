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

package viewmodels.checkAnswers.expenses.officeSupplies

import controllers.journeys.expenses
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OfficeSupplies
import models.journeys.expenses.individualCategories.OfficeSupplies.YesDisallowable
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object OfficeSuppliesDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(OfficeSuppliesPage, Some(businessId))
      .filter(areAnyOfficeSuppliesDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages) =
    for {
      disallowableAmount <- answers.get(OfficeSuppliesDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(OfficeSuppliesAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      expenses.officeSupplies.routes.OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"officeSuppliesDisallowableAmount.title.$userType", formatMoney(allowableAmount)),
      "officeSuppliesDisallowableAmount.change.hidden"
    )

  private def areAnyOfficeSuppliesDisallowable(officeSupplies: OfficeSupplies): Boolean =
    officeSupplies match {
      case YesDisallowable => true
      case _               => false
    }

}
