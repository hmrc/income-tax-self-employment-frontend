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

import base.{CYAControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.UserType
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountSummary, GoodsToSellOrUseAmountSummary}
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAView

class GoodsToSellOrUseCYAControllerSpec extends CYAControllerBaseSpec with CYAOnSubmitControllerBaseSpec[GoodsToSellOrUseJourneyAnswers] {

  override protected lazy val onPageLoadRoute: String = routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url
  override protected lazy val onSubmitRoute: String   = routes.GoodsToSellOrUseCYAController.onSubmit(taxYear, businessId).url

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

  override protected val journeyAnswers: GoodsToSellOrUseJourneyAnswers =
    GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  override protected val journey: Journey = ExpensesGoodsToSellOrUse

  override protected def expectedSummaryList(user: UserType)(implicit messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      GoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, user.toString).value,
      DisallowableGoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, user.toString).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {

    val view = application.injector.instanceOf[GoodsToSellOrUseCYAView]
    view(taxYear, businessId, scenario.userType.toString, summaryList)(request, messages).toString()

  }

}
