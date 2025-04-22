/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys.expenses.professionalFees

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.professionalFees
import models.common.Journey.ExpensesProfessionalFees
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.professionalFees.ProfessionalFeesCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.professionalFees.{ProfessionalFeesAmountSummary, ProfessionalFeesDisallowableAmountSummary}

class ProfessionalFeesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = ProfessionalFeesCYAPage.toString

  private val allowableAmount    = BigDecimal(200.00)
  private val disallowableAmount = BigDecimal(100.00)

  override val journey: Journey = ExpensesProfessionalFees

  def onPageLoadCall: (TaxYear, BusinessId) => Call = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = professionalFees.routes.ProfessionalFeesCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        ProfessionalFeesAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        ProfessionalFeesDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override val submissionData: JsObject = Json.obj(
    "disallowableProfessionalFees"       -> true,
    "professionalFeesAmount"             -> allowableAmount,
    "professionalFeesDisallowableAmount" -> disallowableAmount
  )
  override val testDataCases: List[JsObject] = List(submissionData)

}
