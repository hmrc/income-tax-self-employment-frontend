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

package viewmodels.checkAnswers.nics

import controllers.journeys.nics.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.nics.Class4NonDivingExemptSingleBusinessPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object Class4NonDivingExemptSingleBusinessSummary {

  def row(answers: UserAnswers, userType: UserType, taxYear: TaxYear)(implicit messages: Messages): Option[SummaryListRow] = {

    val remainingBusinesses = Class4NonDivingExemptSingleBusinessPage.remainingBusinesses(answers)

    answers.get(Class4NonDivingExemptSingleBusinessPage, BusinessId.nationalInsuranceContributions).map { idList =>
      val value = idList.headOption
        .collect {
          case id if id == BusinessId.classFourNoneExempt => "No"
          case _                                          => "Yes"
        }
        .getOrElse("No")

      buildRowString(
        value,
        routes.Class4NonDivingExemptSingleBusinessController.onPageLoad(taxYear, CheckMode),
        s"${messages(s"class4NonDivingExemptSingleBusiness.subHeading.$userType")} ${remainingBusinesses.headOption.map(_.tradingName).getOrElse("")}.",
        "class4NonDivingExempt.change.hidden"
      )
    }
  }
}
