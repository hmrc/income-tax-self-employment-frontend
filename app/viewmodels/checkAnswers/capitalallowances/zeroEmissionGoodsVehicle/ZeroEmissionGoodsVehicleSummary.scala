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

package viewmodels.checkAnswers.capitalallowances.zeroEmissionGoodsVehicle

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehiclePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.{AnswerSummary, buildRowBoolean}

object ZeroEmissionGoodsVehicleSummary extends AnswerSummary {

  def row(answers: UserAnswers,
          taxYear: TaxYear,
          businessId: BusinessId,
          userType: UserType,
          rightTextAlign: Boolean = true,
          overrideKeyMessage: Option[String] = None,
          overrideChangeMessage: Option[String] = None)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(ZeroEmissionGoodsVehiclePage, businessId.some)
      .map { answer =>
        buildRowBoolean(
          answer,
          routes.ZeroEmissionGoodsVehicleController.onPageLoad(taxYear, businessId, CheckMode),
          messages(s"zeroEmissionGoodsVehicle.subHeading.cya.$userType", taxYear.startYear.toString, taxYear.endYear.toString),
          "zeroEmissionGoodsVehicle.change.hidden",
          rightTextAlign
        )
      }
}
