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

package viewmodels.checkAnswers.capitalallowances.tailoring

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.tailoring.routes._
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString
import viewsupport.journeys.capitalallowances.tailoring.CapitalAllowancesCYAViewSupport.renderCyaValue

object SelectCapitalAllowancesSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] = {
    val maybeContent = for {
      claim      <- answers.get(ClaimCapitalAllowancesPage, businessId.some)
      allowances <- answers.get(SelectCapitalAllowancesPage, businessId.some)
      content <- Option.when(claim)(allowances match {
        case set if set.isEmpty => messages("site.none")
        case set                => renderCyaValue(set)
      })
    } yield content

    maybeContent.map { content =>
      buildRowString(
        content,
        SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, CheckMode),
        "capitalAllowances.CYA.key2",
        "capitalAllowances.CYA.key2.change.hidden",
        flipKeyToValueWidthRatio = true
      )
    }
  }

}
