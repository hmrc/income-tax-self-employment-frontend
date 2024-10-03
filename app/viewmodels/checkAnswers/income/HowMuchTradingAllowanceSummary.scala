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

package viewmodels.checkAnswers.income

import controllers.journeys.income.{getMaxTradingAllowance, routes}
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.income.HowMuchTradingAllowance
import pages.income.HowMuchTradingAllowancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowString

object HowMuchTradingAllowanceSummary extends MoneyUtils {

  def row(userAnswers: UserAnswers, taxYear: TaxYear, userType: UserType, businessId: BusinessId)(implicit
      messages: Messages): Option[Either[ServiceError, SummaryListRow]] =
    userAnswers.get(HowMuchTradingAllowancePage, Some(businessId)).map { answer =>
      val rowValueOrError = answer match {
        case HowMuchTradingAllowance.Maximum =>
          getMaxTradingAllowance(businessId, userAnswers)
            .map(amount => s"The maximum £${formatMoney(amount)}")

        case HowMuchTradingAllowance.LessThan =>
          Right(messages("common.lowerAmount"))
      }

      for {
        rowValue <- rowValueOrError
      } yield buildRowString(
        rowValue,
        routes.HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode),
        s"howMuchTradingAllowance.checkYourAnswersLabel.$userType",
        "howMuchTradingAllowance.change.hidden",
        rightTextAlign = true
      )
    }

}
