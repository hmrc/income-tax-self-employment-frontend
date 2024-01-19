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

package controllers.journeys.capitalallowances.tailoring

import base.cyaPages.CYAOnPageLoadControllerBaseSpec
import controllers.journeys.capitalallowances.tailoring.routes._
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.capitalallowances.tailoring.CapitalAllowancesCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.capitalallowances.tailoring.{ClaimCapitalAllowancesSummary, SelectCapitalAllowancesSummary}

// TODO Implement CYAOnSubmitControllerBaseSpec in SASS-6724
class CapitalAllowancesCYASpec extends CYAOnPageLoadControllerBaseSpec {

  override val pageHeading: String =
    CapitalAllowancesCYAPage.pageName.value

  override val testDataCases: List[JsObject] =
    List(Json.obj("claimCapitalAllowances" -> true, "selectCapitalAllowances" -> Json.arr("zeroEmissionCar")))

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = CapitalAllowanceCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = CapitalAllowanceCYAController.onPageLoad

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        ClaimCapitalAllowancesSummary.row(userAnswers, taxYear, businessId, userType).value,
        SelectCapitalAllowancesSummary.row(userAnswers, taxYear, businessId).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )
}
