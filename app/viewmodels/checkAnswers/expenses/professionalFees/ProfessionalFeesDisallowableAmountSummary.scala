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

package viewmodels.checkAnswers.expenses.professionalFees

import controllers.journeys.expenses.professionalFees.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.professionalFees._
import pages.expenses.tailoring.individualCategories.DisallowableProfessionalFeesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object ProfessionalFeesDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableProfessionalFeesPage, Some(businessId))
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(ProfessionalFeesDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(ProfessionalFeesAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"professionalFeesDisallowableAmount.title.$userType", allowableAmount),
      "professionalFeesDisallowableAmount.change.hidden"
    )

}
