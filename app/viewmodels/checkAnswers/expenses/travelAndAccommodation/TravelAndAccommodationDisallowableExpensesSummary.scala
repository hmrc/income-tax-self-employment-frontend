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
import models.{CheckMode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.TravelForWork
import models.journeys.expenses.travelAndAccommodation.TravelExpensesDb
import pages.expenses.tailoring.individualCategories.TravelForWorkPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.{buildRowBigDecimal, buildRowString}
import viewmodels.journeys.SummaryListCYA

object TravelAndAccommodationDisallowableExpensesSummary {
  def apply(optTravelExpensesData: Option[TravelExpensesDb], userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(
      implicit messages: Messages): Aliases.SummaryList =
    SummaryListCYA.summaryListOpt(
      List(
        optTravelExpensesData.flatMap(_.totalTravelExpenses).map { answer =>
          buildRowBigDecimal(
            answer,
            routes.TravelAndAccommodationTotalExpensesController.onPageLoad(taxYear, businessId, CheckMode),
            s"travelAndAccommodationTotalExpenses.heading.$userType",
            "travelAndAccommodationTotalExpenses.change.hidden"
          )
        }
      ) ++ (userAnswers.get(TravelForWorkPage, Option(businessId)) match {
        case Some(TravelForWork.YesDisallowable) =>
          List[Option[SummaryListRow]](
            optTravelExpensesData.collect { case TravelExpensesDb(_, _, _, Some(totalTravelExpenses), Some(disallowableTravelExpenses)) =>
              buildRowBigDecimal(
                disallowableTravelExpenses,
                routes.TravelAndAccommodationDisallowableExpensesController.onPageLoad(taxYear, businessId, CheckMode),
                Messages(s"travelAndAccommodationDisallowableExpenses.heading.$userType", totalTravelExpenses),
                "travelAndAccommodationDisallowableExpenses.change.hidden"
              )
            }
          )
        case _ =>
          List.empty[Option[SummaryListRow]]
      })
    )
}
