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

package viewmodels.checkAnswers.expenses.interest

import controllers.journeys.expenses.interest.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.DisallowableInterest
import models.journeys.expenses.individualCategories.DisallowableInterest.{No, Yes}
import pages.expenses.interest._
import pages.expenses.tailoring.individualCategories.DisallowableInterestPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object InterestDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableInterestPage, Some(businessId))
      .filter(areAnyInterestDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(InterestDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(InterestAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.InterestDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"interestDisallowableAmount.title.$userType", allowableAmount),
      "interestDisallowableAmount.change.hidden"
    )

  private def areAnyInterestDisallowable(interestAnswer: DisallowableInterest): Boolean =
    interestAnswer match {
      case Yes => true
      case No  => false
    }

}
