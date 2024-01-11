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

package navigation

import controllers.journeys
import controllers.journeys.income.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.common.AccountingType.{Accrual, Cash}
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.Journey.Income
import models.journeys.income.HowMuchTradingAllowance.{LessThan, Maximum}
import models.journeys.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import pages._
import pages.income._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class IncomeNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => (TaxYear, BusinessId, Option[AccountingType]) => Call = {

    case IncomeNotCountedAsTurnoverPage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(IncomeNotCountedAsTurnoverPage, Some(businessId)) match {
            case Some(true)  => NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case NonTurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)

    case TurnoverIncomeAmountPage => _ => (taxYear, businessId, _) => AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode)

    case AnyOtherIncomePage =>
      userAnswers =>
        (taxYear, businessId, accountingType) =>
          userAnswers.get(AnyOtherIncomePage, Some(businessId)) match {
            case Some(true)                                     => OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) if accountingType contains Accrual => TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) if accountingType contains Cash    => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _                                              => JourneyRecoveryController.onPageLoad()
          }

    case OtherIncomeAmountPage =>
      _ =>
        (taxYear, businessId, accountingType) =>
          accountingType match {
            case Some(Accrual) => TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(Cash)    => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _             => JourneyRecoveryController.onPageLoad()
          }

    case TurnoverNotTaxablePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(TurnoverNotTaxablePage, Some(businessId)) match {
            case Some(true)  => NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(false) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case NotTaxableAmountPage => _ => (taxYear, businessId, _) => TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

    case TradingAllowancePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(TradingAllowancePage, Some(businessId)) match {
            case Some(UseTradingAllowance) => HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(DeclareExpenses)     => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _                         => JourneyRecoveryController.onPageLoad()
          }

    case HowMuchTradingAllowancePage =>
      userAnswers =>
        (taxYear, businessId, _) =>
          userAnswers.get(HowMuchTradingAllowancePage, Some(businessId)) match {
            case Some(LessThan) => TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)
            case Some(Maximum)  => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _              => JourneyRecoveryController.onPageLoad()
          }

    case TradingAllowanceAmountPage =>
      _ => (taxYear, businessId, _) => IncomeCYAController.onPageLoad(taxYear, businessId)

    case IncomeCYAPage =>
      _ => (taxYear, businessId, _) => SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode)

    case _ => _ => (_, _, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (TaxYear, BusinessId) => Call = {

    case IncomeNotCountedAsTurnoverPage =>
      userAnswers =>
        (taxYear, businessId) =>
          userAnswers.get(IncomeNotCountedAsTurnoverPage, Some(businessId)) match {
            case Some(true)  => NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(false) => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case AnyOtherIncomePage =>
      userAnswers =>
        (taxYear, businessId) =>
          userAnswers.get(AnyOtherIncomePage, Some(businessId)) match {
            case Some(true)  => OtherIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(false) => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case TurnoverNotTaxablePage =>
      userAnswers =>
        (taxYear, businessId) =>
          userAnswers.get(TurnoverNotTaxablePage, Some(businessId)) match {
            case Some(true)  => NotTaxableAmountController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(false) => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _           => JourneyRecoveryController.onPageLoad()
          }

    case TradingAllowancePage =>
      userAnswers =>
        (taxYear, businessId) =>
          userAnswers.get(TradingAllowancePage, Some(businessId)) match {
            case Some(UseTradingAllowance) => HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(DeclareExpenses)     => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _                         => JourneyRecoveryController.onPageLoad()
          }

    case HowMuchTradingAllowancePage =>
      userAnswers =>
        (taxYear, businessId) =>
          userAnswers.get(HowMuchTradingAllowancePage, Some(businessId)) match {
            case Some(LessThan) => TradingAllowanceAmountController.onPageLoad(taxYear, businessId, CheckMode)
            case Some(Maximum)  => IncomeCYAController.onPageLoad(taxYear, businessId)
            case _              => JourneyRecoveryController.onPageLoad()
          }

    case IncomeCYAPage =>
      _ => (taxYear, businessId) => journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode)

    case _ => _ => (taxYear, businessId) => IncomeCYAController.onPageLoad(taxYear, businessId)
  }

  def nextPage(page: Page,
               mode: Mode,
               userAnswers: UserAnswers,
               taxYear: TaxYear,
               businessId: BusinessId,
               accountingType: Option[AccountingType] = None): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)(taxYear, businessId, accountingType)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)(taxYear, businessId)
    }

}
