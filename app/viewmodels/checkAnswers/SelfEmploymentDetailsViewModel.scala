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

package viewmodels.checkAnswers

import models.mdtp.BusinessData
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, FormatStyle}

object SelfEmploymentDetailsViewModel {

  def buildSummaryList(business: BusinessData, isAgent: Boolean)(implicit messages: Messages): SummaryList = {
    SummaryList(
      rows = Seq(
        row("tradingName", business.tradingName.getOrElse(""), Some(isAgent)),
        row("typeOfBusiness", business.typeOfBusiness, Some(isAgent)),
        row("accountingType", business.accountingType.getOrElse("")),
        row("startDate", handleDateString(business.commencementDate), Some(isAgent)),
        row("linkedToConstructionIndustryScheme", "No"),
        row("fosterCare", "No", Some(isAgent)),
        row("farmerOrMarketGardener", "No", Some(isAgent)),
        row("profitFromLiteraryOrCreativeWorks", "No", Some(isAgent))
      ),
      classes = "govuk-!-margin-bottom-7")
  }

  private def row(rowKey: String, rowContent: String, userIsAgent: Option[Boolean] = None)
                 (implicit messages: Messages): SummaryListRow = {
    val agentIndividual = userIsAgent match {
      case None => ""
      case Some(isAgent) => if (isAgent) ".agent" else ".individual"
    }
    SummaryListRowViewModel(
      key = Key(
        content = s"checkYourSelfEmploymentDetails.$rowKey$agentIndividual",
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = rowContent,
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(ActionItemViewModel(messages("site.change"), "#"))
    )
  }

  private def handleDateString(date: Option[String]): String = try {
      LocalDate.parse(date.getOrElse("")).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    } catch {
      case _: Throwable => ""
    }
}
