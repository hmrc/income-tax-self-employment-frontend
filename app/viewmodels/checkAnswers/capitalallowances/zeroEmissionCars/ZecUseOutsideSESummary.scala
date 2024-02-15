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

package viewmodels.checkAnswers.capitalallowances.zeroEmissionCars

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.zeroEmissionCars.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE.DifferentAmount
import pages.capitalallowances.zeroEmissionCars.{ZecUseOutsideSEPage, ZecUseOutsideSEPercentagePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object ZecUseOutsideSESummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] = {
    def buildRow(answer: String) = buildRowString(
      answer,
      routes.ZecUseOutsideSEController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"zecUseOutsideSE.title.$userType"),
      "zecUseOutsideSE.change.hidden",
      rightTextAlign = true
    )
    val answer     = answers.get(ZecUseOutsideSEPage, businessId.some)
    val percentage = answers.get(ZecUseOutsideSEPercentagePage, businessId.some)
    (answer, percentage) match {
      case (Some(DifferentAmount), Some(percentage)) => buildRow(s"${percentage.toString}%").some
      case (Some(value), _)                          => buildRow(value.toString).some
      case _                                         => None
    }
  }

}
