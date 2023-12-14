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

package controllers.journeys.expenses.goodsToSellOrUse

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.goodsToSellOrUse
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountSummary, GoodsToSellOrUseAmountSummary}

class GoodsToSellOrUseCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = GoodsToSellOrUseCYAPage.toString

  def onPageLoadCall: (TaxYear, BusinessId) => Call = goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onSubmit

  override protected val journey: Journey = ExpensesGoodsToSellOrUse

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      GoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
      DisallowableGoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, userType).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override val submissionData = Json.obj(
    "goodsToSellOrUse"                   -> "yesDisallowable",
    "goodsToSellOrUseAmount"             -> 100.00,
    "disallowableGoodsToSellOrUseAmount" -> 100.00
  )
  override val testDataCases: List[JsObject] = List(submissionData)

}
