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
import forms.OverMaxError
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, TaxYear, UserType}
import models.journeys.expenses.travelAndAccommodation.TravelExpensesDb
import pages.travelAndAccommodation.TravelAndAccommodationTotalExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationTotalExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TravelAndAccommodationTotalExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    answersService: AnswersService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: CurrencyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: TravelAndAccommodationTotalExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = (userType: UserType) =>
    formProvider(TravelAndAccommodationTotalExpensesPage, userType, prefix = Some("travelAndAccommodationTotalExpenses"))

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)
      answersService.getAnswers[TravelExpensesDb](ctx).map { optTravelExpensesData =>
        val preparedForm = optTravelExpensesData.flatMap(_.totalTravelExpenses).fold(form(request.userType))(form(request.userType).fill)
        Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, ExpensesTravelForWork)
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              oldAnswers <- answersService.getAnswers[TravelExpensesDb](ctx)
              newData <- answersService.replaceAnswers(
                ctx = ctx,
                data = oldAnswers
                  .getOrElse(TravelExpensesDb())
                  .copy(totalTravelExpenses = Some(value))
              )
            } yield NotImplemented
        )
  }
}
