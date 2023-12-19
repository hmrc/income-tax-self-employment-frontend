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

package viewmodels.checkAnswers.tradeDetails

import models.common.UserType
import models.domain.BusinessData
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import viewmodels.journeys.SummaryListCYA

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, FormatStyle}

object SelfEmploymentDetailsViewModel {

  def buildSummaryList(business: BusinessData, userType: UserType)(implicit messages: Messages): SummaryList =
    SummaryListCYA.summaryList(
      List(
        row("tradingName", business.tradingName.getOrElse(""), Some(userType)),
        row("typeOfBusiness", business.typeOfBusiness, Some(userType)),
        row("accountingType", business.accountingType.getOrElse("")),
        row("startDate", handleDateString(business.commencementDate), Some(userType)),
        row("linkedToConstructionIndustryScheme", "No"),
        row("fosterCare", "No", Some(userType)),
        row("farmerOrMarketGardener", "No", Some(userType)),
        row("profitFromLiteraryOrCreativeWorks", "No", Some(userType))
      )
    )

  private def row(rowKey: String, rowContent: String, userType: Option[UserType] = None)(implicit messages: Messages): SummaryListRow = {
    val agentIndividual = userType match {
      case None           => ""
      case Some(userType) => s".$userType"
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

  private def handleDateString(date: Option[String]): String = try
    LocalDate.parse(date.getOrElse("")).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
  catch {
    case _: Throwable => ""
  }
}
