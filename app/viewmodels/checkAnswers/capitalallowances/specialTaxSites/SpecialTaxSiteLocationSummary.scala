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

package viewmodels.checkAnswers.capitalallowances.specialTaxSites

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocation
import pages.capitalallowances.specialTaxSites.SpecialTaxSiteLocationPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object SpecialTaxSiteLocationSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(SpecialTaxSiteLocationPage, businessId.some)
      .map { answer =>
        buildRowString(
          formatLocationAnswer(answer),
          routes.SpecialTaxSiteLocationController.onPageLoad(taxYear, businessId, CheckMode),
          messages("specialTaxSiteLocation.title"),
          "specialTaxSiteLocation.change.hidden",
          rightTextAlign = true
        )
      }

  private def formatLocationAnswer(answer: SpecialTaxSiteLocation): String = {
    val buildingName   = answer.buildingName.map(_ + "<br>").getOrElse("")
    val buildingNumber = answer.buildingNumber.map(_ + "<br>").getOrElse("")
    val postCode       = answer.postCode
    buildingName + buildingNumber + postCode
  }
}
