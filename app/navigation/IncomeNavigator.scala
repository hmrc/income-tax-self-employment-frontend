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

import controllers.journeys.income.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import pages._
import pages.income._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class IncomeNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => (Int, String, Option[Boolean]) => Call = {

    case IncomeNotCountedAsTurnoverPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (userAnswers.get(IncomeNotCountedAsTurnoverPage).getOrElse(false)) {
            NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
          } else {
            TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
          }

    case NonTurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case TurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode)

    case AnyOtherIncomePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (userAnswers.get(AnyOtherIncomePage).getOrElse(false)) { // TODO should all the getOrElse default to ERROR?
            OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
          } else {
            TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode) // TODO what if cash/accrual
          }

    case OtherIncomeAmountPage =>
      _ =>
        (taxYear, businessId, isAccrual) =>
          if (isAccrual.getOrElse(false)) {
            TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
          } else {
            TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
          }

    case TurnoverNotTaxablePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (userAnswers.get(TurnoverNotTaxablePage).getOrElse(false)) {
            NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode)
          } else {
            TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
          }

    case NotTaxableAmountPage => _ => (taxYear, businessId, _) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case TradingAllowancePage => _ => (taxYear, businessId, _) => HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case HowMuchTradingAllowancePage => _ => (taxYear, businessId, _) => TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case TradingAllowanceAmountPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (isComplete(userAnswers)) {
            CheckYourIncomeController.onPageLoad(taxYear, businessId)
          } else JourneyRecoveryController.onPageLoad()
    case IncomeCYAPage =>
      _ => (taxYear, businessId, _) => SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode)

    case SectionCompletedStatePage => _ => (taxYear, _, _) => TaskListController.onPageLoad(taxYear)

    case _ => _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (Int, String, Option[Boolean]) => Call = {

    case _ =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (isComplete(userAnswers))
            CheckYourIncomeController.onPageLoad(taxYear, businessId)
          else JourneyRecoveryController.onPageLoad()

    case _ => _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: String, isAccrual: Option[Boolean] = None): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId, isAccrual)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId, isAccrual)
    }

  private def isComplete(userAnswers: UserAnswers): Boolean = true // TODO replace this with a check to ensure user answers are actually completed?
}
