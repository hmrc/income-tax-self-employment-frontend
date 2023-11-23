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
import forms.expenses.tailoring.OfficeSuppliesFormProvider
import models.Mode
import models.common.ModelUtils.userType
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.OfficeSuppliesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.OfficeSuppliesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeSuppliesController @Inject() (override val messagesApi: MessagesApi,
                                          selfEmploymentService: SelfEmploymentService,
                                          sessionRepository: SessionRepository,
                                          navigator: ExpensesTailoringNavigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: OfficeSuppliesFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: OfficeSuppliesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) map {
        case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
        case Right(accountingType) =>
          val preparedForm = request.userAnswers.get(OfficeSuppliesPage, Some(businessId)) match {
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
          val form = formProvider(userType(request.user.isAgent))
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId, accountingType))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(OfficeSuppliesPage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(OfficeSuppliesPage, mode, updatedAnswers, taxYear, businessId))
            )
      }
  }

}
