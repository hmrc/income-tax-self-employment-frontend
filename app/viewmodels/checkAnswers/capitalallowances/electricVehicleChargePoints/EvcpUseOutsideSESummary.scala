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

package viewmodels.checkAnswers.capitalallowances.electricVehicleChargePoints

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.electricVehicleChargePoints.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpUseOutsideSE.DifferentAmount
import pages.capitalallowances.electricVehicleChargePoints.{EvcpUseOutsideSEPage, EvcpUseOutsideSEPercentagePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object EvcpUseOutsideSESummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] = {
    def buildRow(answer: String) = buildRowString(
      answer,
      routes.EvcpUseOutsideSEController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"evcpUseOutsideSE.title.$userType"),
      "evcpUseOutsideSE.change.hidden",
      rightTextAlign = true
    )
    val answer     = answers.get(EvcpUseOutsideSEPage, businessId.some)
    val percentage = answers.get(EvcpUseOutsideSEPercentagePage, businessId.some)
    (answer, percentage) match {
      case (Some(DifferentAmount), Some(percentage)) => buildRow(s"${percentage.toString}%").some
      case (Some(value), _)                          => buildRow(value.toString).some
      case _                                         => None
    }
  }

}
