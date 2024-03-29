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

import base.SpecBase
import controllers.journeys.income.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import models.common.AccountingType
import models.database.UserAnswers
import models.journeys.Journey.Income
import models.journeys.income.HowMuchTradingAllowance
import models.journeys.income.HowMuchTradingAllowance.{LessThan, Maximum}
import models.journeys.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import pages._
import pages.income._

class IncomeNavigatorSpec extends SpecBase {

  val navigator = new IncomeNavigator

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "Income Not Counted As Turnover Page must go to the" - {
        "Non-Turnover Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(IncomeNotCountedAsTurnoverPage, true, Some(businessId)).success.value

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Turnover Income Amount page when answer is 'No'" in {

          val userAnswers = UserAnswers(userAnswersId).set(IncomeNotCountedAsTurnoverPage, false, Some(businessId)).success.value

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, NormalMode, userAnswers, taxYear, businessId) mustBe
            TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Non-Turnover Income Amount Page must go to the Turnover Income Amount page" in {

        navigator.nextPage(NonTurnoverIncomeAmountPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "Turnover Income Amount Page must go to the Any Other Income page" in {

        navigator.nextPage(TurnoverIncomeAmountPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          AnyOtherIncomeController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "Any Other Income Page must go to the" - {
        "Other Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, true, Some(businessId)).success.value

          navigator.nextPage(AnyOtherIncomePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            OtherIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Turnover Not Taxable page when answer is 'No' and accounting type is 'ACCRUAL'" in {

          val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, false, Some(businessId)).success.value

          navigator.nextPage(AnyOtherIncomePage, NormalMode, userAnswers, taxYear, businessId, Some(AccountingType.Accrual)) mustBe
            TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Trading Allowance page when answer is 'No' and accounting type is 'CASH'" in {

          val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, false, Some(businessId)).success.value

          navigator.nextPage(AnyOtherIncomePage, NormalMode, userAnswers, taxYear, businessId, Some(AccountingType.Cash)) mustBe
            TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(AnyOtherIncomePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Other Income Amount Page must go to the" - {
        "Turnover Not Taxable page when accounting type is 'ACCRUAL'" in {

          navigator.nextPage(OtherIncomeAmountPage, NormalMode, emptyUserAnswers, taxYear, businessId, Some(AccountingType.Accrual)) mustBe
            TurnoverNotTaxableController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Trading Allowance page when accounting type is 'CASH'" in {

          navigator.nextPage(OtherIncomeAmountPage, NormalMode, emptyUserAnswers, taxYear, businessId, Some(AccountingType.Cash)) mustBe
            TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there is no accounting type" in {

          navigator.nextPage(OtherIncomeAmountPage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Turnover Not Taxable Page must go to the" - {
        "Non-Turnover Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TurnoverNotTaxablePage, true, Some(businessId)).success.value

          navigator.nextPage(TurnoverNotTaxablePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Trading Allowance page when answer is 'No'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TurnoverNotTaxablePage, false, Some(businessId)).success.value

          navigator.nextPage(TurnoverNotTaxablePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(TurnoverNotTaxablePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Not Taxable Amount Page must go to the Trading Allowance page" in {

        navigator.nextPage(NotTaxableAmountPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
      }

      "Trading Allowance Page must go to the" - {
        "How Much Trading Allowance page when answer is 'UseTradingAllowance'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TradingAllowancePage, UseTradingAllowance, Some(businessId)).success.value

          navigator.nextPage(TradingAllowancePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Trading Allowance page when answer is 'DeclareExpenses'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TradingAllowancePage, DeclareExpenses, Some(businessId)).success.value

          navigator.nextPage(TradingAllowancePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(TradingAllowancePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "How Much Trading Allowance Page must go to the" - {
        "Trading Allowance Amount page when answer is 'LessThan'" in {

          val userAnswers =
            UserAnswers(userAnswersId).set[HowMuchTradingAllowance](HowMuchTradingAllowancePage, LessThan, Some(businessId)).success.value

          navigator.nextPage(HowMuchTradingAllowancePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)
        }
        "Trading Allowance page when answer is 'Maximum'" in {

          val userAnswers = UserAnswers(userAnswersId).set(HowMuchTradingAllowancePage, Maximum, Some(businessId)).success.value

          navigator.nextPage(HowMuchTradingAllowancePage, NormalMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(HowMuchTradingAllowancePage, NormalMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Trading Allowance Amount Page must go to the Income CYA page" in {

        navigator.nextPage(TradingAllowanceAmountPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          IncomeCYAController.onPageLoad(taxYear, businessId)
      }

      "Income CYA page must go to the Section Completed page with Income journey" in {

        navigator.nextPage(IncomeCYAPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          SectionCompletedStateController.onPageLoad(taxYear, businessId, Income.toString, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "Income Not Counted As Turnover Page must go to the" - {
        "Non-Turnover Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(IncomeNotCountedAsTurnoverPage, true, Some(businessId)).success.value

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            NonTurnoverIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "CYA page when answer is 'No'" in {

          val userAnswers = UserAnswers(userAnswersId).set(IncomeNotCountedAsTurnoverPage, false, Some(businessId)).success.value

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, CheckMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(IncomeNotCountedAsTurnoverPage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Any Other Income Page must go to the" - {
        "Other Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, true, Some(businessId)).success.value

          navigator.nextPage(AnyOtherIncomePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            OtherIncomeAmountController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "CYA page when answer is 'No'" in {

          val userAnswers = UserAnswers(userAnswersId).set(AnyOtherIncomePage, false, Some(businessId)).success.value

          navigator.nextPage(AnyOtherIncomePage, CheckMode, userAnswers, taxYear, businessId, Some(AccountingType.Accrual)) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(AnyOtherIncomePage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Turnover Not Taxable Page must go to the" - {
        "Non-Turnover Income Amount page when answer is 'Yes'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TurnoverNotTaxablePage, true, Some(businessId)).success.value

          navigator.nextPage(TurnoverNotTaxablePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            NotTaxableAmountController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "CYA page when answer is 'No'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TurnoverNotTaxablePage, false, Some(businessId)).success.value

          navigator.nextPage(TurnoverNotTaxablePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(TurnoverNotTaxablePage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "Trading Allowance Page must go to the" - {
        "How Much Trading Allowance page when answer is 'UseTradingAllowance'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TradingAllowancePage, UseTradingAllowance, Some(businessId)).success.value

          navigator.nextPage(TradingAllowancePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "Trading Allowance page when answer is 'DeclareExpenses'" in {

          val userAnswers = UserAnswers(userAnswersId).set(TradingAllowancePage, DeclareExpenses, Some(businessId)).success.value

          navigator.nextPage(TradingAllowancePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(TradingAllowancePage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "How Much Trading Allowance Page must go to the" - {
        "Trading Allowance Amount page when answer is 'LessThan'" in {

          val userAnswers = UserAnswers(userAnswersId).set(HowMuchTradingAllowancePage, LessThan, Some(businessId)).success.value

          navigator.nextPage(HowMuchTradingAllowancePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            TradingAllowanceAmountController.onPageLoad(taxYear, businessId, CheckMode)
        }
        "Trading Allowance page when answer is 'Maximum'" in {

          val userAnswers = UserAnswers(userAnswersId).set(HowMuchTradingAllowancePage, Maximum, Some(businessId)).success.value

          navigator.nextPage(HowMuchTradingAllowancePage, CheckMode, userAnswers, taxYear, businessId) mustBe
            IncomeCYAController.onPageLoad(taxYear, businessId)
        }
        "Journey Recovery page when there are no UserAnswers for this page" in {

          navigator.nextPage(HowMuchTradingAllowancePage, CheckMode, emptyUserAnswers, taxYear, businessId) mustBe
            JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from any other Income journey page to the 'Check your details' page" in {

        navigator.nextPage(NonTurnoverIncomeAmountPage, CheckMode, UserAnswers("id"), taxYear, businessId) mustBe
          IncomeCYAController.onPageLoad(taxYear, businessId)
      }
    }
  }

}
