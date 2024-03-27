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

package viewmodels.checkAnswers.capitalallowances.structuresBuildingsAllowance

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.structuresBuildingsAllowance.routes
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.capitalallowances.structuresBuildingsAllowance.NewStructuresBuildingsList
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowBigDecimal

object StructuresBuildingsClaimedAmountSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(NewStructuresBuildingsList, businessId.some)
      .map { sites =>
        buildRowBigDecimal(
          sites.map(_.newStructureBuildingClaimingAmount.getOrElse(BigDecimal(0))).sum,
          routes.StructuresBuildingsNewStructuresController.onPageLoad(taxYear, businessId),
          messages("newSpecialTaxSites.cya"),
          "newSpecialTaxSites.change.hidden"
        )
      }
}
