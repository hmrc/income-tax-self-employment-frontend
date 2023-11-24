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
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.tailoring.ProfessionalServiceExpensesFormProvider
import models.Mode
import models.common.ModelUtils.userType
import models.common.TaxYear
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.ProfessionalServiceExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.ProfessionalServiceExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProfessionalServiceExpensesController @Inject() (override val messagesApi: MessagesApi,
                                                       sessionRepository: SessionRepository,
                                                       selfEmploymentService: SelfEmploymentService,
                                                       navigator: ExpensesTailoringNavigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: ProfessionalServiceExpensesFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ProfessionalServiceExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) map {
        case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
        case Right(accountingType) =>
          val preparedForm = request.userAnswers.get(ProfessionalServiceExpensesPage, Some(businessId)) match {
            case None        => formProvider(userType(request.user.isAgent))
            case Some(value) => formProvider(userType(request.user.isAgent)).fill(value)
          }

          Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId, accountingType))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) =>
          formProvider(userType(request.user.isAgent))
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId, accountingType))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ProfessionalServiceExpensesPage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ProfessionalServiceExpensesPage, mode, updatedAnswers, taxYear, businessId))
            )
      }
  }

}
