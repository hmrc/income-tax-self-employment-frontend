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

package controllers.journeys.expenses.tailoring

import controllers.actions._
import forms.expenses.tailoring.ProfessionalServiceExpensesFormProvider
import models.Mode
import models.database.UserAnswers
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.ProfessionalServiceExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.ProfessionalServiceExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProfessionalServiceExpensesController @Inject() (override val messagesApi: MessagesApi,
                                                       sessionRepository: SessionRepository,
                                                       navigator: ExpensesTailoringNavigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       formProvider: ProfessionalServiceExpensesFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ProfessionalServiceExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(ProfessionalServiceExpensesPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, taxYear, businessId))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, taxYear, businessId))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(ProfessionalServiceExpensesPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ProfessionalServiceExpensesPage, mode, updatedAnswers, taxYear, businessId))
      )
  }

}
