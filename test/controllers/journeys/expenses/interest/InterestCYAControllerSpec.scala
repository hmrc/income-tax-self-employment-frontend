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

package controllers.journeys.expenses.interest

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.interest
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.common.Journey
import models.common.Journey.ExpensesInterest
import pages.expenses.interest.InterestCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.interest.{InterestAmountSummary, InterestDisallowableAmountSummary}

class InterestCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = InterestCYAPage.toString

  private val allowableAmount    = BigDecimal(200.00)
  private val disallowableAmount = BigDecimal(100.00)

  override val journey: Journey = ExpensesInterest

  def onPageLoadCall: (TaxYear, BusinessId) => Call = interest.routes.InterestCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = interest.routes.InterestCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        InterestAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        InterestDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override val submissionData: JsObject = Json.obj(
    "disallowableInterest"       -> true,
    "interestAmount"             -> allowableAmount,
    "interestDisallowableAmount" -> disallowableAmount
  )
  override val testDataCases: List[JsObject] = List(submissionData)

}
