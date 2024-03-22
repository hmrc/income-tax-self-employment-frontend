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

package viewmodels.journeys.capitalallowances.specialTaxSites

import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildChangeRemoveRow
import viewmodels.journeys.SummaryListCYA

object NewTaxSitesViewModel {

  def getNewSitesRows(sites: List[NewSpecialTaxSite], taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): SummaryList = {
    def convertToSummaryListRow(siteWithIndex: (NewSpecialTaxSite, Int))(implicit messages: Messages): Option[SummaryListRow] =
      siteWithIndex._1.specialTaxSiteLocation map { location =>
        buildChangeRemoveRow(
          siteWithIndex._1.newSiteClaimingAmount.toString(),
          s"${location.buildingName.getOrElse(location.buildingNumber.getOrElse(""))} ${location.postCode}",
          routes.SiteSummaryController.onPageLoad(taxYear, businessId, siteWithIndex._2),
          "CHANGE",
          routes.SiteSummaryController.onPageLoad(taxYear, businessId, siteWithIndex._2),
          "REMOVE"
        )
      }

    SummaryListCYA.summaryListOpt(sites.zipWithIndex.map(convertToSummaryListRow), Some("hmrc-list-with-actions hmrc-list-with-actions--short"))
  }

}
