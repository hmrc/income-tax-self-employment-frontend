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

package viewmodels.checkAnswers.expenses.goodsToSellOrUse

import controllers.journeys.expenses
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.GoodsToSellOrUse
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.tailoring.individualCategories.GoodsToSellOrUsePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.checkAnswers.buildRowBigDecimal

object DisallowableGoodsToSellOrUseAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(GoodsToSellOrUsePage, Some(businessId))
      .filter(areAnyGoodsToSellOrUseDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages) =
    for {
      disallowableAmount <- answers.get(DisallowableGoodsToSellOrUseAmountPage, Some(businessId))
      allowableAmount    <- answers.get(GoodsToSellOrUseAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      expenses.goodsToSellOrUse.routes.DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"disallowableGoodsToSellOrUseAmount.title.$userType", allowableAmount),
      "disallowableGoodsToSellOrUseAmount.change.hidden"
    )

  private def areAnyGoodsToSellOrUseDisallowable(goodsToSellOrUse: GoodsToSellOrUse): Boolean =
    goodsToSellOrUse match {
      case YesDisallowable => true
      case _               => false
    }

}
