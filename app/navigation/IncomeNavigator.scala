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
          userAnswers.get(IncomeNotCountedAsTurnoverPage) match {
            case Some(true)  => NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case _ =>
              JourneyRecoveryController.onPageLoad()
          }

    case NonTurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case TurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode)

    case AnyOtherIncomePage =>
      userAnswers =>
        (taxYear, businessId, optIsAccrual) =>
          userAnswers.get(AnyOtherIncomePage) match {
            case Some(true)                                   => OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) if optIsAccrual.getOrElse(false) => TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) if !optIsAccrual.getOrElse(true) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _                                            => JourneyRecoveryController.onPageLoad()
          }

    case OtherIncomeAmountPage =>
      _ =>
        (taxYear, businessId, optIsAccrual) =>
          optIsAccrual match {
            case Some(true)  => TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case TurnoverNotTaxablePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(TurnoverNotTaxablePage) match {
            case Some(true)  => NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case NotTaxableAmountPage => _ => (taxYear, businessId, _) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case TradingAllowancePage => _ => (taxYear, businessId, _) => HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case HowMuchTradingAllowancePage => _ => (taxYear, businessId, _) => TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case TradingAllowanceAmountPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          if (isComplete(userAnswers)) CheckYourIncomeController.onPageLoad(taxYear, businessId)
          else JourneyRecoveryController.onPageLoad()

    case IncomeCYAPage =>
      _ =>
        (taxYear, businessId, _) =>
          SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode) // TODO 5840 isComplete checks?

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
