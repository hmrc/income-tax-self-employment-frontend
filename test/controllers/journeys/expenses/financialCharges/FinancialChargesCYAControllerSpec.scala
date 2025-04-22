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

package controllers.journeys.expenses.financialCharges

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.financialCharges.routes._
import models.common.Journey.ExpensesFinancialCharges
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.financialCharges.FinancialChargesCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.financialCharges.{FinancialChargesAmountSummary, FinancialChargesDisallowableAmountSummary}

class FinancialChargesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String =
    FinancialChargesCYAPage.pageName.value

  override val submissionData: JsObject =
    Json.obj("disallowableOtherFinancialCharges" -> true, "financialChargesAmount" -> 123.00, "financialChargesDisallowableAmount" -> 123.00)

  override val testDataCases: List[JsObject] =
    List(submissionData)

  override def onPageLoadCall: (TaxYear, BusinessId) => Call =
    FinancialChargesCYAController.onPageLoad

  override def onSubmitCall: (TaxYear, BusinessId) => Call =
    FinancialChargesCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        FinancialChargesAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        FinancialChargesDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override val journey: Journey = ExpensesFinancialCharges
}
