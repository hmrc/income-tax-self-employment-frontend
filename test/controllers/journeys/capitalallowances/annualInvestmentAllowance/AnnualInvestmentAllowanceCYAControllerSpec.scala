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

package controllers.journeys.capitalallowances.annualInvestmentAllowance

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.CapitalAllowancesAnnualInvestmentAllowance
import pages.Page
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.capitalallowances.annualInvestmentAllowance.{AnnualInvestmentAllowanceAmountSummary, AnnualInvestmentAllowanceSummary}

class AnnualInvestmentAllowanceCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = Page.cyaCheckYourAnswersHeading

  override val submissionData: JsObject = Json.obj(
    "annualInvestmentAllowance"       -> true,
    "annualInvestmentAllowanceAmount" -> 500.00
  )

  override val testDataCases: List[JsObject] = List(submissionData)

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.AnnualInvestmentAllowanceCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.AnnualInvestmentAllowanceCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        AnnualInvestmentAllowanceSummary.row(userAnswers, taxYear, businessId, userType).value,
        AnnualInvestmentAllowanceAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override val journey: Journey = CapitalAllowancesAnnualInvestmentAllowance

}
