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

package viewmodels.journeys.capitalallowances.structuresBuildingsAllowance

import controllers.journeys.capitalallowances.specialTaxSites.routes
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.buildChangeRemoveRow
import viewmodels.journeys.SummaryListCYA

object NewStructuresBuildingsViewModel {

  def getNewStructuresSummaryRows(sites: List[NewStructureBuilding], taxYear: TaxYear, businessId: BusinessId)(implicit
      messages: Messages): SummaryList = {
    def convertToSummaryListRow(siteWithIndex: (NewStructureBuilding, Int))(implicit messages: Messages): Option[SummaryListRow] =
      siteWithIndex._1.newStructureBuildingLocation.flatMap { location =>
        siteWithIndex._1.newStructureBuildingClaimingAmount.map { amount =>
          buildChangeRemoveRow(
            s"£${formatMoney(amount)}",
            s"${location.buildingName.getOrElse(location.buildingNumber.getOrElse(""))} ${location.postCode}",
            routes.SiteSummaryController.onPageLoad(taxYear, businessId, siteWithIndex._2),
            "hidden.CHANGE.message", // TODO get these hidden messages
            routes.SiteSummaryController.onPageLoad(taxYear, businessId, siteWithIndex._2),
            "hidden.REMOVE.message"
          )
        }
      }

    SummaryListCYA.summaryListOpt(sites.zipWithIndex.map(convertToSummaryListRow), Some("hmrc-list-with-actions hmrc-list-with-actions--short"))
  }

}
