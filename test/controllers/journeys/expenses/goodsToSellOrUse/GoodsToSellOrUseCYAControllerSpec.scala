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

import base.{CYAOnPageLoadControllerSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.goodsToSellOrUse
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountSummary, GoodsToSellOrUseAmountSummary}
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAView

class GoodsToSellOrUseCYAControllerSpec extends CYAOnPageLoadControllerSpec with CYAOnSubmitControllerBaseSpec {

  override val pageName: String = GoodsToSellOrUseCYAPage.toString

  def onPageLoadCall: (TaxYear, BusinessId) => Call = goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = goodsToSellOrUse.routes.GoodsToSellOrUseCYAController.onSubmit

  private val userAnswerData = Json
    .parse(s"""
         |{
         |  "$businessId": {
         |    "goodsToSellOrUse": "yesDisallowable",
         |    "goodsToSellOrUseAmount": 100.00,
         |    "disallowableGoodsToSellOrUseAmount": 100.00
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  override protected val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)

  override protected val journey: Journey = ExpensesGoodsToSellOrUse

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      GoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, userType.toString).value,
      DisallowableGoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, userType.toString).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseCYAView]
    view(taxYear, businessId, userType.toString, summaryList)(request, messages).toString()
  }

  override val testDataCases: List[JsObject] =
    List(
      Json.obj(
        "goodsToSellOrUse"                   -> "yesDisallowable",
        "goodsToSellOrUseAmount"             -> 100.00,
        "disallowableGoodsToSellOrUseAmount" -> 100.00
      )
    )

}
