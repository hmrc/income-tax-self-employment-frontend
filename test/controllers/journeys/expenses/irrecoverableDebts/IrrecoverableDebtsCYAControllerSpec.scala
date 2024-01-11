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

package controllers.journeys.expenses.irrecoverableDebts

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import pages.expenses.irrecoverableDebts.IrrecoverableDebtsCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountSummary, IrrecoverableDebtsDisallowableAmountSummary}

class IrrecoverableDebtsCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = IrrecoverableDebtsCYAPage.pageName.value

  override lazy val testDataCases: List[JsObject] =
    List(
      Json.obj(
        "disallowableIrrecoverableDebts"       -> "yes",
        "irrecoverableDebtsAmount"             -> 123.00,
        "irrecoverableDebtsDisallowableAmount" -> 123.00
      ))

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.IrrecoverableDebtsCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.IrrecoverableDebtsCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        IrrecoverableDebtsAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        IrrecoverableDebtsDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  protected val journey: Journey = Journey.ExpensesIrrecoverableDebts

  protected val submissionData: JsObject = Json.obj(
    "financialExpenses"                    -> List("irrecoverableDebts"),
    "disallowableIrrecoverableDebts"       -> "yesDisallowable",
    "irrecoverableDebtsAmount"             -> 200.00,
    "irrecoverableDebtsDisallowableAmount" -> 100.00
  )
}
