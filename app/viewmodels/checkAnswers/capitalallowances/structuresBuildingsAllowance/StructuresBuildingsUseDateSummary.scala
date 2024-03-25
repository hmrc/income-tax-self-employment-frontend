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

import controllers.journeys.capitalallowances.structuresBuildingsAllowance.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowLocalDate

import java.time.LocalDate

object StructuresBuildingsUseDateSummary {

  def row(answer: LocalDate, taxYear: TaxYear, businessId: BusinessId, index: Int)(implicit messages: Messages): SummaryListRow =
    buildRowLocalDate(
      answer,
      routes.StructuresBuildingsQualifyingUseDateController.onPageLoad(taxYear, businessId, index, CheckMode),
      messages("structuresBuildingsQualifyingUseDate.title"),
      "constructionStartDate.change.hidden", // TODO change
      rightTextAlign = true
    )
}
