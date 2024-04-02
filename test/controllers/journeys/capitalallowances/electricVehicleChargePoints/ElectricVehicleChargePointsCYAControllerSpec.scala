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

package controllers.journeys.capitalallowances.electricVehicleChargePoints

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.CapitalAllowancesElectricVehicleChargePoints
import models.journeys.capitalallowances.electricVehicleChargePoints._
import pages.capitalallowances.tailoring.CapitalAllowancesCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.capitalallowances.electricVehicleChargePoints._

class ElectricVehicleChargePointsCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = CapitalAllowancesCYAPage.pageName.value

  override val submissionData: JsObject = Json.obj(
    "evcpAllowance"               -> true,
    "chargePointTaxRelief"        -> true,
    "amountSpentOnEvcp"           -> 400.00,
    "evcpOnlyForSelfEmployment"   -> false,
    "evcpUsedOutsideSE"           -> EvcpUseOutsideSE.Fifty.toString,
    "evcpUsedOutsideSEPercentage" -> 50,
    "evcpHowMuchDoYouWantToClaim" -> EvcpHowMuchDoYouWantToClaim.FullCost.toString,
    "evcpClaimAmount"             -> 200.00
  )

  override val testDataCases: List[JsObject] = List(submissionData)

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.ElectricVehicleChargePointsCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.ElectricVehicleChargePointsCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        EvcpAllowanceSummary.row(userAnswers, taxYear, businessId, userType).value,
        ChargePointTaxReliefSummary.row(userAnswers, taxYear, businessId, userType).value,
        AmountSpentOnEvcpSummary.row(userAnswers, taxYear, businessId).value,
        EvcpOnlyForSelfEmploymentSummary.row(userAnswers, taxYear, businessId, userType).value,
        EvcpUseOutsideSESummary.row(userAnswers, taxYear, businessId, userType).value,
        EvcpHowMuchDoYouWantToClaimSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override val journey: Journey = CapitalAllowancesElectricVehicleChargePoints
}
