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

package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MoneyUtilsSpec extends AnyWordSpec with Matchers with MoneyUtils {

  "formatMoney" when {
    "intent is to retain integers" should {
      "retain them" in {
        val bigDecimals = List(1, 12, 123)

        val expectedResults = List("1", "12", "123")

        bigDecimals.map(formatMoney(_, addDecimalForWholeNumbers = false)) should contain theSameElementsInOrderAs expectedResults
      }
    }
    "intent is to turn integers into 2dp" should {
      "convert them" in {
        val bigDecimals = List(1, 10, 100, 1000)

        val expectedResults = List("1.00", "10.00", "100.00", "1,000.00")

        bigDecimals.map(formatMoney(_)) should contain theSameElementsInOrderAs expectedResults
      }

    }
    "amount is not an integer" should {
      "retain 2dp when 2dp is provided" in {
        val bigDecimals = List(1.23, 10.99, 0.98, 0.01)

        val expectedResults = List("1.23", "10.99", "0.98", "0.01")

        bigDecimals.map(formatMoney(_)) should contain theSameElementsInOrderAs expectedResults
      }
      "add commas every thousand" in {
        val bigDecimals = List(100.22, 1000.22, 10000.22, 100000.22, 1000000.22)

        val expectedResults = List("100.22", "1,000.22", "10,000.22", "100,000.22", "1,000,000.22")

        bigDecimals.map(formatMoney(_)) should contain theSameElementsInOrderAs expectedResults
      }
      "format to 2dp when 1dp is provided" in {
        val bigDecimals = List(1.5, 100.1)

        val expectedResults = List("1.50", "100.10")

        bigDecimals.map(formatMoney(_)) should contain theSameElementsInOrderAs expectedResults
      }

    }
  }

  "formatPosNegMoneyWithPounds" should {
    "convert a BigDecimal amount to a String, prefixed with a '£' symbol" when {
      "the amount is zero or greater" should {
        "be shown with no decimal places when an amount is whole number" in {
          assert(formatPosNegMoneyWithPounds(0) == "£0")
          assert(formatPosNegMoneyWithPounds(-0) == "£0")
          assert(formatPosNegMoneyWithPounds(100.00) == "£100")
        }
        "be shown to two decimal places when an amount has pence" in {
          assert(formatPosNegMoneyWithPounds(100.2) == "£100.20")
          assert(formatPosNegMoneyWithPounds(01.011) == "£1.01")
        }
      }
      "the amount is a negative number" should {
        "be wrapped in brackets, without the minus symbol" in {
          assert(formatPosNegMoneyWithPounds(-20) == "(£20)")
          assert(formatPosNegMoneyWithPounds(-02.02) == "(£2.02)")
          assert(formatPosNegMoneyWithPounds(-200.2) == "(£200.20)")
        }
      }
    }
  }

  "formatDecimals" when {
    "the amount is a whole number" should {
      "display without decimal places" in {
        val bigDecimals     = List(0, 100, 100.0, 10000, -200, -500.00)
        val expectedResults = List("0", "100", "100", "10,000", "-200", "-500")
        bigDecimals.map(formatDecimals(_)) should contain theSameElementsInOrderAs expectedResults
      }
    }

    "the amount has decimal places" should {
      "show exactly two decimal places" in {
        val bigDecimals = List(
          100.5, 100.50, 100.499, 100.501, 0.1, -123.45, -78.9
        )
        val expectedResults = List(
          "100.50",
          "100.50",
          "100.50",
          "100.50",
          "0.10",
          "-123.45",
          "-78.90"
        )
        bigDecimals.map(formatDecimals(_)) should contain theSameElementsInOrderAs expectedResults
      }

      "add commas as thousand separators" in {
        val bigDecimals     = List(1234.56, 12345.67, 987654.32)
        val expectedResults = List("1,234.56", "12,345.67", "987,654.32")
        bigDecimals.map(formatDecimals(_)) should contain theSameElementsInOrderAs expectedResults
      }
    }
  }
}
