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

package controllers.journeys.expenses

import controllers.actions._
import forms.expenses.ProfessionalServiceExpensesFormProvider
import models.ModelUtils.userType
import models.{Mode, UserAnswers}
import navigation.ExpensesNavigator
import pages.expenses.ProfessionalServiceExpensesPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ContentStringViewModel.buildLegendHeadingWithHintString
import views.html.journeys.expenses.ProfessionalServiceExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProfessionalServiceExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: ExpensesNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    formProvider: ProfessionalServiceExpensesFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ProfessionalServiceExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val businessId = "SJPR05893938418"

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>

    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(ProfessionalServiceExpensesPage, Some(businessId)) match {
      case None        => formProvider(userType(request.user.isAgent))
      case Some(value) => formProvider(userType(request.user.isAgent)
      ).fill(value)
    }

    Ok(view(preparedForm, mode, userType(request.user.isAgent),legendContent))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    formProvider(userType(request.user.isAgent))
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, authUserType = userType(request.user.isAgent), legendContent))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(ProfessionalServiceExpensesPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ProfessionalServiceExpensesPage, mode, updatedAnswers))
      )
  }

  private def legendContent(implicit messages: Messages) = buildLegendHeadingWithHintString(
    s"professionalServiceExpenses.subheading",
    "site.selectAllThatApply",
    headingClasses = "govuk-fieldset__legend govuk-fieldset__legend--m"
  )

}
