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

import controllers.journeys.expenses.goodsToSellOrUse.routes.DisallowableGoodsToSellOrUseAmountController
import models.CheckMode
import models.database.UserAnswers
import models.journeys.expenses.GoodsToSellOrUse
import models.journeys.expenses.GoodsToSellOrUse.YesDisallowable
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.tailoring.GoodsToSellOrUsePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import utils.MoneyUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DisallowableGoodsToSellOrUseAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: Int, businessId: String, userType: String)(implicit messages: Messages): Option[SummaryListRow] =
    for {
      goodsToSellOrUse <- answers.get(GoodsToSellOrUsePage, Some(businessId))
      if areAnyGoodsToSellOrUseDisallowable(goodsToSellOrUse)
      disallowableAmount <- answers.get(DisallowableGoodsToSellOrUseAmountPage, Some(businessId))
      allowableAmount    <- answers.get(GoodsToSellOrUseAmountPage, Some(businessId))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"disallowableGoodsToSellOrUseAmount.title.$userType", allowableAmount),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = s"£${formatMoney(disallowableAmount)}",
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel("site.change", DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages("disallowableGoodsToSellOrUseAmount.change.hidden"))
      )
    )

  private def areAnyGoodsToSellOrUseDisallowable(goodsToSellOrUse: GoodsToSellOrUse): Boolean =
    goodsToSellOrUse match {
      case YesDisallowable => true
      case _               => false
    }

}
