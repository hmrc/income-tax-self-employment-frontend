/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses.travelAndAccommodation

import controllers.journeys.expenses.travelAndAccommodation.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import pages.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object VehicleExpenseTypeSummary {

  def row(
      answers: UserAnswers,
      taxYear: TaxYear,
      businessId: BusinessId,
      userType: UserType
  )(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(TravelAndAccommodationExpenseTypePage, Some(businessId))
      .map { answer =>
        buildRowString(
          answer.toString,
          routes.TravelAndAccommodationExpenseTypeController.onPageLoad(taxYear, businessId, CheckMode),
          messages(s"travelAndAccommodationExpenseType.${answer.toString}.$userType"),
          s"travelAndAccommodationExpenseType.title.$userType",
          rightTextAlign = true
        )
      }

}

//def row(taxYear: TaxYear, userType: UserType, businessId: BusinessId, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
//  answers.get(SelfEmploymentAbroadPage, Some(businessId)).map { answer =>
//  buildRowBoolean(
//  answer,
//  routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, CheckMode),
//  s"selfEmploymentAbroad.title.$userType",
//  "selfEmploymentAbroad.change.hidden"
//  )
//  }

//    answers.get(TravelAndAccommodationExpenseTypePage).map { answer =>
//      val value = ValueViewModel(
//        HtmlContent(
//          HtmlFormat.escape(messages(s"travelAndAccommodationExpenseType.$answer"))
//        )
//      )

//      SummaryListRowViewModel(
//        key = "travelAndAccommodationExpenseType.checkYourAnswersLabel",
//        value = value,
//        actions = Seq(
//          ActionItemViewModel("site.change", routes.TravelAndAccommodationExpenseTypeController.onPageLoad(taxYear, businessId, CheckMode).url)
//            .withVisuallyHiddenText(messages("travelAndAccommodationExpenseType.change.hidden"))
//        )
//      )
//    }
//  }
