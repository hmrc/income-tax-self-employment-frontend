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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import base.cyaPages.CYAOnPageLoadControllerBaseSpec
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocation
import pages.Page
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.capitalallowances.structuresBuildingsAllowance._

class StructuresBuildingsCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec {

  override val pageHeading: String = Page.cyaCheckYourAnswersHeading

  override val testDataCases: List[JsObject] = List(
    Json.obj(
      "structuresBuildingsAllowance"     -> true,
      "structuresBuildingsEligibleClaim" -> true,
      "newStructuresBuildings" -> List(Json.obj(
        "qualifyingUse"                                   -> Some("2022-03-02"),
        "newStructureBuildingQualifyingExpenditureAmount" -> Some(BigDecimal(1000)),
        "newStructureBuildingLocation"                    -> Some(StructuresBuildingsLocation(Some("name"), Some("number"), "GU84NB")),
        "newStructureBuildingClaimingAmount"              -> Some(BigDecimal(2000))
      )),
      "structuresBuildingsClaimed"               -> true,
      "structuresBuildingsPreviousClaimUse"      -> true,
      "structuresBuildingsPreviousClaimedAmount" -> BigDecimal(1000)
    ))

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.StructuresBuildingsCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.StructuresBuildingsCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        StructuresBuildingsAllowanceSummary.row(userAnswers, taxYear, businessId, userType).value,
        StructuresBuildingsEligibleClaimSummary.row(userAnswers, taxYear, businessId, userType).value,
        StructuresBuildingsClaimedAmountSummary.row(userAnswers, taxYear, businessId).value,
        StructuresBuildingsClaimedSummary.row(userAnswers, taxYear, businessId, userType).value,
        StructuresBuildingsPreviousClaimUseSummary.row(userAnswers, taxYear, businessId, userType).value,
        StructuresBuildingsPreviousClaimedAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

}
