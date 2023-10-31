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
import forms.expenses.WorkFromBusinessPremisesFormProvider
import models.ModelUtils.userType
import models.{Mode, UserAnswers}
import navigation.ExpensesNavigator
import pages.expenses.WorkFromBusinessPremisesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ContentStringViewModel.buildLegendHeadingWithHintString
import views.html.journeys.expenses.WorkFromBusinessPremisesView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WorkFromBusinessPremisesController @Inject() (override val messagesApi: MessagesApi,
                                                    sessionRepository: SessionRepository,
                                                    navigator: ExpensesNavigator,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: WorkFromBusinessPremisesFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: WorkFromBusinessPremisesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val businessId = "SJPR05893938418"
  val taxYear    = LocalDate.now.getYear

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(WorkFromBusinessPremisesPage, Some(businessId)) match {
      case None        => formProvider(userType(request.user.isAgent))
      case Some(value) => formProvider(userType(request.user.isAgent)).fill(value)
    }
    val radioContentString = buildLegendHeadingWithHintString(
      WorkFromBusinessPremisesPage,
      Some(userType(request.user.isAgent)),
      headingExtraClasses = "govuk-!-margin-bottom-2")

    Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId, radioContentString))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val radioContentString = buildLegendHeadingWithHintString(
      WorkFromBusinessPremisesPage,
      Some(userType(request.user.isAgent)),
      headingExtraClasses = "govuk-!-margin-bottom-2")
    formProvider(userType(request.user.isAgent))
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId, radioContentString))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.getOrElse(UserAnswers(request.userId)).set(WorkFromBusinessPremisesPage, value, Some(businessId)))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WorkFromBusinessPremisesPage, mode, updatedAnswers))
      )
  }

}
