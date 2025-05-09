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

import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

trait MoneyUtils {

  def formatMoney(amount: BigDecimal, addDecimalForWholeNumbers: Boolean = true): String = {
    val numberFormat  = NumberFormat.getNumberInstance(Locale.UK)
    val decimalFormat = new DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(Locale.UK))

    if (amount.isWhole && !addDecimalForWholeNumbers) {
      numberFormat.format(amount)
    } else {
      decimalFormat.format(amount)
    }
  }

  private def swapMinusForBrackets(amount: String): String = s"(${amount.replace("-", "")})"

  def formatPosNegMoneyWithPounds(amount: BigDecimal): String = {
    val formattedAmount = s"£${formatMoney(amount, addDecimalForWholeNumbers = false)}"
    if (amount < 0) swapMinusForBrackets(formattedAmount) else formattedAmount
  }

  def formatSumMoneyNoNegative(amounts: List[BigDecimal]): String =
    s"£${formatMoney(amounts.sum, addDecimalForWholeNumbers = false).replace("-", "")}"

  def formatDecimals(amount: BigDecimal): String = {
    val decimalFormatWhole    = new DecimalFormat("#,##0", new java.text.DecimalFormatSymbols(Locale.UK))
    val decimalFormatFraction = new DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(Locale.UK))

    if (amount % 1 == 0) {
      decimalFormatWhole.format(amount)
    } else {
      decimalFormatFraction.format(amount)
    }
  }

}

object MoneyUtils extends MoneyUtils
