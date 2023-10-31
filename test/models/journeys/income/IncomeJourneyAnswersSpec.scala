package models.journeys.income

import models.{HowMuchTradingAllowance, TradingAllowance}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IncomeJourneyAnswersSpec extends AnyWordSpec with Matchers {

  private val allowedIncomeJourneyAnswers = IncomeJourneyAnswers(
    incomeNotCountedAsTurnover = true,
    nonTurnoverIncomeAmount = Some(123.45),
    turnoverIncomeAmount = 123.45,
    anyOtherIncome = true,
    otherIncomeAmount = Some(123.45),
    turnoverNotTaxable = Some(true),
    notTaxableAmount = Some(123.45),
    tradingAllowance = TradingAllowance.UseTradingAllowance,
    howMuchTradingAllowance = Some(HowMuchTradingAllowance.LessThan),
    tradingAllowanceAmount = Some(123.45)
  )

  "IncomeJourneyAnswers" when {
    "answering `No` to the IncomeNotCountedAsTurnover question" must {
      "forbid the presence of an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(incomeNotCountedAsTurnover = false)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "answering `Yes` to the IncomeNotCountedAsTurnover question" must {
      "enforce an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(nonTurnoverIncomeAmount = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }

    }
    "answering `No` to the AnyOtherIncome question" must {
      "forbid the presence of an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(anyOtherIncome = false)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "answering `Yes` to the AnyOtherIncome question" must {
      "enforce of an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(otherIncomeAmount = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }

    }

    "answering `No` to the TurnoverNotTaxable question" must {
      "forbid the presence of an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(turnoverNotTaxable = Some(false))

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "answering `Yes` to the TurnoverNotTaxable question" must {
      "enforce an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(notTaxableAmount = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }

    }
    "not answering the TurnoverNotTaxable question" must {
      "enforce an amount" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(turnoverNotTaxable = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }

    "choosing to declare expenses when prompted the TradingAllowance question" must {
      "forbid the presence of an answer to how much of your trading allowance question" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(tradingAllowance = TradingAllowance.DeclareExpenses)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "choosing to use the trading allowance when prompted the TradingAllowance question" must {
      "enforce an answer to the how much of your trading allowance question" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(howMuchTradingAllowance = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }

    }
    "choosing to use the maximum trading allowance when prompted the HowMuchTradingAllowance question" must {
      "forbid the presence of an answer to the follow up amount question" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(howMuchTradingAllowance = Some(HowMuchTradingAllowance.Maximum))

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "not answering the HowMuchTradingAllowance question" must {
      "forbid the presence of an answer to the follow up amount question" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(howMuchTradingAllowance = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
    "choosing to use less than the maximum trading allowance when prompted the HowMuchTradingAllowance question" must {
      "enforce an answer to the follow up amount question" in {
        lazy val answers = allowedIncomeJourneyAnswers.copy(tradingAllowanceAmount = None)

        val result = intercept[IllegalArgumentException](answers)

        result shouldBe a[IllegalArgumentException]
      }
    }
  }

}
