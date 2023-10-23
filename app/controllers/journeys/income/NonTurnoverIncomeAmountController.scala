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

package controllers.journeys.income

import controllers.actions._
import forms.income.NonTurnoverIncomeAmountFormProvider
import models.Mode
import navigation.Navigator
import pages.income.NonTurnoverIncomeAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.NonTurnoverIncomeAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonTurnoverIncomeAmountController @Inject() (override val messagesApi: MessagesApi,
                                                   selfEmploymentService: SelfEmploymentService,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: NonTurnoverIncomeAmountFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: NonTurnoverIncomeAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NonTurnoverIncomeAmountPage, Some(businessId)) match {
        case None        => formProvider(authUserType(request.user.isAgent))
        case Some(value) => formProvider(authUserType(request.user.isAgent)).fill(value)
      }

      Ok(view(preparedForm, mode, authUserType(request.user.isAgent), taxYear, businessId))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(authUserType(request.user.isAgent))
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, authUserType(request.user.isAgent), taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NonTurnoverIncomeAmountPage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NonTurnoverIncomeAmountPage, mode, updatedAnswers, taxYear, Some(businessId)))
        )
  }

  private def authUserType(isAgent: Boolean): String = if (isAgent) "agent" else "individual"

}
