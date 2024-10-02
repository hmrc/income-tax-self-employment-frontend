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
import models.common.BusinessId.classFourOtherExemption
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.domain.BusinessData
import models.journeys.nics.ExemptionReason
import pages.nics.{Class4DivingExemptPage, Class4ExemptionReasonPage, Class4NICsPage, Class4NonDivingExemptPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object Class4NonDivingExemptSummary {

//  def row(answers: UserAnswers, businesses: Seq[BusinessData], userType: UserType, taxYear: TaxYear)(implicit
//                                                                                                     messages: Messages): Option[SummaryListRow] = {
//    answers.get(Class4NonDivingExemptPage, BusinessId.nationalInsuranceContributions).map { idList =>
//      buildRowString(
//        formatBusinessTradingNameAnswers(idList, businesses),
//        routes.Class4NonDivingExemptController.onPageLoad(taxYear, CheckMode),
//        s"class4NonDivingExempt.subHeading.cya.$userType",
//        "class4NonDivingExempt.change.hidden"
//      )
//    }

  // if the user has a business id for Non diving exempt then it should display the reason on the page
  // if the user from a different business id is exempt from paying from a different reason, then the reason should be displayed on the page -
  // (if no summary means all of the business data is covered by Diving)
  // if the user has all business ids saved as Diving exempt then the object will return None - that means  Do not display the reason on the summary page.

  def row(answers: UserAnswers, businesses: Seq[BusinessData], userType: UserType, taxYear: TaxYear)(implicit
                                                                                                     messages: Messages): Option[SummaryListRow] = {
    val buildRow = (idList: List[BusinessId]) =>
      buildRowString(
        formatBusinessTradingNameAnswers(idList, businesses),
        routes.Class4NonDivingExemptController.onPageLoad(taxYear, CheckMode),
        s"class4NonDivingExempt.subHeading.cya.$userType",
        "class4NonDivingExempt.change.hidden"
      )

    val divingIds: Option[List[BusinessId]] = answers.get(Class4DivingExemptPage, BusinessId.nationalInsuranceContributions)
    val nonDivingIds: Option[List[BusinessId]] = answers.get(Class4NonDivingExemptPage, BusinessId.nationalInsuranceContributions)
    val hasClass4Answers: Option[Boolean] = answers.get(Class4NICsPage, BusinessId.nationalInsuranceContributions)
    val numOfDivingIds: Int = divingIds.map(_.length).getOrElse(0)
    val allIdsAreDiving: Boolean = businesses.length == numOfDivingIds

    (nonDivingIds, allIdsAreDiving, hasClass4Answers) match {
      case (Some(idList), false, Some(true)) if idList.nonEmpty => Some(buildRow(idList))
      case (_, false, Some(true))                               => Some(buildRow(List(classFourOtherExemption)))
      case _                                => None
    }
  }
}

// None -
// List[Ids]
// List(message) - Class 4 multiple business with exempt reason. And if there are 0 Non diving ids, however we need to make sure that Not all id's are diving
//