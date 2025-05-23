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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions._
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.TravelExpensesDb
import navigation.TravelAndAccommodationNavigator
import pages.expenses.TravelAndAccommodationDisallowableExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationDisallowableExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TravelAndAccommodationDisallowableExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    answersService: AnswersService,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: CurrencyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: TravelAndAccommodationDisallowableExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = (userType: UserType, totalExpenses: BigDecimal) =>
    formProvider(
      TravelAndAccommodationDisallowableExpensesPage,
      userType,
      maxValue = totalExpenses,
      minValue = 0,
      prefix = Some("travelAndAccommodationDisallowableExpenses"),
      args = Seq(totalExpenses.toString())
    )

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)
      answersService.getAnswers[TravelExpensesDb](ctx).map { optTravelExpensesData =>
        optTravelExpensesData.flatMap(_.totalTravelExpenses) match {
          case Some(totalExpenses) =>
            val preparedForm = optTravelExpensesData
              .flatMap(_.disallowableTravelExpenses)
              .fold(form(request.userType, totalExpenses))(form(request.userType, totalExpenses).fill)
            Ok(view(preparedForm, mode, request.userType, taxYear, businessId, totalExpenses))
          case _ => Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
        }
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)
      answersService.getAnswers[TravelExpensesDb](ctx) flatMap { optTravelExpenses =>
        optTravelExpenses.flatMap(_.totalTravelExpenses) match {
          case Some(totalTravelExpenses) =>
            form(request.userType, totalTravelExpenses)
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, totalTravelExpenses))),
                value =>
                  for {
                    newData <- answersService.replaceAnswers(
                      ctx = ctx,
                      data = optTravelExpenses
                        .getOrElse(TravelExpensesDb())
                        .copy(disallowableTravelExpenses = Some(value))
                    )
                  } yield Redirect(navigator
                    .nextTravelExpensesPage(TravelAndAccommodationDisallowableExpensesPage, mode, newData, taxYear, businessId, request.userAnswers))
              )
          case _ => Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
        }
      }

  }
}
