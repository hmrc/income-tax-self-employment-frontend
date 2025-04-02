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
import pages.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseTypePage, TravelForWorkYourVehiclePage}
import play.api.i18n.Messages
import play.twirl.api.Html
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
      .get(TravelAndAccommodationExpenseTypePage, businessId)
      .flatMap {
        case set if set.isEmpty =>
          None
        case set =>
          val orderedOptions = List(
            "myOwnVehicle",
            "leasedVehicles",
            "publicTransportAndOtherAccommodation"
          )

          val sortedOptions = set.toList.sortBy { option =>
            orderedOptions.indexOf(option.toString)
          }

          val htmlContent = sortedOptions
            .map { expenseType =>
              val messageKey = expenseType match {
                case TravelAndAccommodationExpenseType.MyOwnVehicle =>
                  s"travelAndAccommodationExpenseType.${expenseType.toString}.$userType"
                case _ =>
                  s"travelAndAccommodationExpenseType.${expenseType.toString}.common"
              }
              Html(s"<div>${messages(messageKey)}</div>")
            }
            .mkString("")

          Some(
            buildRowString(
              answer = htmlContent,
              callLink = routes.TravelAndAccommodationExpenseTypeController.onPageLoad(taxYear, businessId, CheckMode),
              keyMessage = messages(s"travelAndAccommodationExpenseType.title.$userType"),
              changeMessage = s"travelAndAccommodationExpenseType.title.$userType",
              rightTextAlign = true
            )
          )
      }
}
