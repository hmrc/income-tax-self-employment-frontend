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

package navigation

import controllers.journeys.expenses.goodsToSellOrUse.routes._
import controllers.journeys.expenses.officeSupplies.routes._
import controllers.journeys.routes.SectionCompletedStateController
import controllers.standard.routes._
import models._
import models.database.UserAnswers
import models.journeys.Journey.{ExpensesGoodsToSellOrUse, ExpensesOfficeSupplies}
import pages._
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage, GoodsToSellOrUseCYAPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesCYAPage, OfficeSuppliesDisallowableAmountPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ExpensesNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Int => String => Call = {

    case OfficeSuppliesAmountPage =>
      _ => taxYear => businessId => OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

    case OfficeSuppliesCYAPage =>
      _ => taxYear => businessId => SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesOfficeSupplies.toString, NormalMode)

    case GoodsToSellOrUseAmountPage =>
      _ => taxYear => businessId => DisallowableGoodsToSellOrUseAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case DisallowableGoodsToSellOrUseAmountPage => _ => taxYear => businessId => GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId)

    case GoodsToSellOrUseCYAPage =>
      _ => taxYear => businessId => SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesGoodsToSellOrUse.toString, NormalMode)

    case _ => _ => _ => _ => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Int => String => Call = {
    case OfficeSuppliesAmountPage | OfficeSuppliesDisallowableAmountPage =>
      _ => taxYear => businessId => OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => _ => _ => JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: String): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(businessId)
    }

}
