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
import models.domain.BusinessData
import pages.nics.Class4DivingExemptPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object Class4DivingExemptSummary {

  def row(answers: UserAnswers, businesses: Seq[BusinessData], userType: UserType, taxYear: TaxYear)(implicit
      messages: Messages): Option[SummaryListRow] =
    answers.get(Class4DivingExemptPage, BusinessId.nationalInsuranceContributions).map { idList =>
      buildRowString(
        formatBusinessTradingNameAnswers(idList, businesses),
        routes.Class4DivingExemptController.onPageLoad(taxYear, CheckMode),
        s"class4DivingExempt.subHeading.cya.$userType",
        "class4DivingExempt.change.hidden"
      )
    }
}
