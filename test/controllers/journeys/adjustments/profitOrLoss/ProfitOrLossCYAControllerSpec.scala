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

package controllers.journeys.adjustments.profitOrLoss

import base.cyaPages.CYAOnPageLoadControllerBaseSpec
import models.CheckMode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.adjustments.WhichYearIsLossReported
import pages.Page
import pages.adjustments.profitOrLoss.{GoodsAndServicesAmountPage, GoodsAndServicesForYourOwnUsePage, PreviousUnusedLossesPage, UnusedLossAmountPage}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.adjustments.WhichYearIsLossReportedSummary
import viewmodels.checkAnswers.{BigDecimalSummary, BooleanSummary}

class ProfitOrLossCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec {

  override val pageHeading: String = Page.cyaCheckYourAnswersHeading

  private val goodsAndServicesAmount  = BigDecimal(100)
  private val unusedLossAmount        = BigDecimal(200.00)
  private val whichYearIsLossReported = WhichYearIsLossReported.Year2018to2019.toString

  val data: JsObject = Json.obj(
    "goodsAndServicesForYourOwnUse" -> true,
    "goodsAndServicesAmount"        -> goodsAndServicesAmount,
    "previousUnusedLosses"          -> true,
    "unusedLossAmount"              -> unusedLossAmount,
    "whichYearIsLossReported"       -> whichYearIsLossReported,
    "accountingType"                -> "CASH"
  )

  override val testDataCases: List[JsObject] = List(data)

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.ProfitOrLossCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.ProfitOrLossCYAController.onSubmit

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = {

    val accountingType                      = userAnswers.getAccountingType(businessId)
    val accountingTypePrefix                = if (accountingType == AccountingType.Cash) ".cash" else ""
    val goodsAndServicesAmountKeyMessage    = Messages(s"goodsAndServicesAmount.subHeading.cya$accountingTypePrefix.$userType")
    val goodsAndServicesAmountChangeMessage = Messages(s"goodsAndServicesAmount.change$accountingTypePrefix.hidden")

    val previousUnusedLossesSummaryKeyMessage =
      Messages(s"previousUnusedLosses.subHeading.cya.$userType", userAnswers.getTraderName(businessId).value)
    val previousUnusedLossesSummaryChangeMessage = Messages(s"previousUnusedLosses.change.hidden", userAnswers.getTraderName(businessId).value)
    SummaryList(
      rows = List(
        new BooleanSummary(
          GoodsAndServicesForYourOwnUsePage,
          routes.GoodsAndServicesForYourOwnUseController.onPageLoad(taxYear, businessId, CheckMode))
          .row(userAnswers, taxYear, businessId, userType)
          .value,
        new BigDecimalSummary(GoodsAndServicesAmountPage, routes.GoodsAndServicesAmountController.onPageLoad(taxYear, businessId, CheckMode))
          .row(
            userAnswers,
            taxYear,
            businessId,
            userType,
            overrideKeyMessage = Some(goodsAndServicesAmountKeyMessage),
            overrideChangeMessage = Some(goodsAndServicesAmountChangeMessage)
          )
          .value,
        new BooleanSummary(PreviousUnusedLossesPage, routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, CheckMode))
          .row(
            userAnswers,
            taxYear,
            businessId,
            userType,
            overrideKeyMessage = Some(previousUnusedLossesSummaryKeyMessage),
            overrideChangeMessage = Some(previousUnusedLossesSummaryChangeMessage)
          )
          .value,
        new BigDecimalSummary(UnusedLossAmountPage, routes.UnusedLossAmountController.onPageLoad(taxYear, businessId, CheckMode))
          .row(userAnswers, taxYear, businessId, userType)
          .value,
        WhichYearIsLossReportedSummary.row(userAnswers, userType, taxYear, businessId).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )
  }
}
