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

package controllers.journeys.expenses.advertisingOrMarketing

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.advertisingOrMarketing
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.common.Journey
import models.common.Journey.ExpensesAdvertisingOrMarketing
import pages.expenses.advertisingOrMarketing.AdvertisingOrMarketingCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.advertisingOrMarketing.{AdvertisingAmountSummary, AdvertisingDisallowableAmountSummary}

class AdvertisingCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = AdvertisingOrMarketingCYAPage.toString

  private val allowableAmount    = BigDecimal(200.00)
  private val disallowableAmount = BigDecimal(100.00)

  def onPageLoadCall: (TaxYear, BusinessId) => Call = advertisingOrMarketing.routes.AdvertisingCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = advertisingOrMarketing.routes.AdvertisingCYAController.onSubmit

  override protected val journey: Journey = ExpensesAdvertisingOrMarketing

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      AdvertisingAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
      AdvertisingDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override val submissionData = Json.obj(
    "advertisingOrMarketing"                   -> "yesDisallowable",
    "advertisingOrMarketingAmount"             -> allowableAmount,
    "advertisingOrMarketingDisallowableAmount" -> disallowableAmount
  )
  override val testDataCases: List[JsObject] = List(submissionData)
}
