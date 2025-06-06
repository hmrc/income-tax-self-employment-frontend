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
import models.NormalMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.TravelForWork
import pages.expenses.tailoring.individualCategories.TravelForWorkPage
import pages.expenses.travelAndAccommodation.{
  DisallowableTransportAndAccommodationPage,
  PublicTransportAndAccommodationExpensesPage,
  TravelAndAccommodationExpenseTypePage
}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.{buildRowBigDecimal, buildRowString}
import viewmodels.journeys.SummaryListCYA

object PublicTransportAndAccommodationExpensesSummary {
  def apply(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Aliases.SummaryList =
    SummaryListCYA.summaryListOpt(
      List(
        userAnswers.get(TravelAndAccommodationExpenseTypePage, Some(businessId)).map { _ =>
          buildRowString(
            messages("travelAndAccommodationExpenseType.publicTransportAndOtherAccommodation.common"),
            routes.TravelAndAccommodationExpenseTypeController.onPageLoad(taxYear, businessId, NormalMode),
            s"travelAndAccommodationExpenseType.title.$userType",
            "travelAndAccommodationExpenseType.change.hidden"
          )
        },
        userAnswers.get(PublicTransportAndAccommodationExpensesPage, Some(businessId)).map { answer =>
          buildRowBigDecimal(
            answer,
            routes.PublicTransportAndAccommodationExpensesController.onPageLoad(taxYear, businessId, NormalMode),
            s"publicTransportAndAccommodationExpenses.title.$userType",
            "publicTransportAndAccommodationExpenses.change.hidden"
          )
        }
      ) ++ (userAnswers.get(TravelForWorkPage, Option(businessId)) match {
        case Some(TravelForWork.YesDisallowable) =>
          List[Option[SummaryListRow]](
            userAnswers.get(DisallowableTransportAndAccommodationPage, Some(businessId)).map { answer =>
              buildRowBigDecimal(
                answer,
                routes.DisallowableTransportAndAccommodationController.onPageLoad(taxYear, businessId, NormalMode),
                getDisallowableMessage(userAnswers, userType, businessId),
                "disallowableTransportAndAccommodation.change.hidden"
              )
            }
          )
        case _ =>
          List.empty[Option[SummaryListRow]]
      })
    )

  private def getDisallowableMessage(userAnswers: UserAnswers, userType: UserType, businessId: BusinessId)(implicit messages: Messages): String =
    userAnswers
      .get(PublicTransportAndAccommodationExpensesPage, Some(businessId))
      .map { amount =>
        Messages(s"disallowableTransportAndAccommodation.heading.$userType", amount)
      }
      .getOrElse("")
}
