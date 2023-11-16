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

package controllers.journeys.expenses.repairsandmaintenance

import cats.data.EitherT
import controllers.actions._
import controllers.handleResult
import controllers.standard.{routes => genRoutes}
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepairsAndMaintenanceAmountController @Inject() (
    override val messagesApi: MessagesApi,
    selfEmploymentService: SelfEmploymentService,
    sessionRepository: SessionRepository,
    navigator: ExpensesNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: RepairsAndMaintenanceAmountFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: RepairsAndMaintenanceAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  // TODO Int => TaxYear in play route, same for businessId
  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val form = formProvider(request.userType)
    val preparedForm = request.answers
      .get(RepairsAndMaintenanceAmountPage, Some(businessId))
      .fold(form)(form.fill)

    Ok(view(preparedForm, mode, TaxYear(taxYear), BusinessId(businessId)))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      // TODO Fix in SASS-6116re
//      form
//        .bindFromRequest()
//        .fold(
//          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, taxYear, businessId))),
//          value =>
//            for {
//              updatedAnswers <- Future.fromTry(request.userAnswers.set(RepairsAndMaintenanceAmountPage, value))
//              _              <- sessionRepository.set(updatedAnswers)
//            } yield Redirect(navigator.nextPage(RepairsAndMaintenanceAmountPage, mode, updatedAnswers, taxYear, businessId))
//        )
      Redirect(navigator.nextPage(RepairsAndMaintenanceAmountPage, mode, request.userAnswers, taxYear, businessId))
  }

}
