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

package models.journeys.adjustments

import models.common.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.time.Year

sealed trait WhichYearIsLossReported

object WhichYearIsLossReported extends Enumerable.Implicits {

  case object currentTaxYearMinusFour  extends WithName(generateYears().last) with WhichYearIsLossReported
  case object currentTaxYearMinusThree extends WithName(generateYears()(3)) with WhichYearIsLossReported
  case object currentTaxYearMinusTwo   extends WithName(generateYears()(2)) with WhichYearIsLossReported
  case object currentTaxYearMinusOne   extends WithName(generateYears()(1)) with WhichYearIsLossReported
  case object currentTaxYear           extends WithName(generateYears().head) with WhichYearIsLossReported

  val values: Seq[WhichYearIsLossReported] = Seq(
    currentTaxYearMinusFour,
    currentTaxYearMinusThree,
    currentTaxYearMinusTwo,
    currentTaxYearMinusOne,
    currentTaxYear
  )

  def options()(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"whichYearIsLossReported.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[WhichYearIsLossReported] =
    Enumerable(values.map(v => v.toString -> v): _*)

  private def generateYears(upTo: Int = 5): Seq[String] = {
    val currentTaxYear   = Year.now.getValue
    val taxYearMinusFive = currentTaxYear - upTo + 1

    val yearRange = for {
      year <- taxYearMinusFive to currentTaxYear
    } yield s"${year - 2}to${year - 1}"
    yearRange.toList
  }

}
