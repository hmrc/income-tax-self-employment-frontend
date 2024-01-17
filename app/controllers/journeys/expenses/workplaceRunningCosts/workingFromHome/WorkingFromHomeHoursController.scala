/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.standard.routes
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider.WorkingFromHomeHoursFormModel
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.i18n._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TaxYearHelper.taxYearCutoffDate
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursView

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkingFromHomeHoursController @Inject() (override val messagesApi: MessagesApi,
                                                service: SelfEmploymentService,
                                                navigator: ExpensesNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: WorkingFromHomeHoursView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      (for {
        business <- service.getBusiness(request.nino, businessId, request.mtditid).left.map(_ => Redirect(routes.JourneyRecoveryController.onPageLoad()))
        startDate <- business.head.commencementDate.toRight(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      } yield (business, startDate)) map {
        case (business, startDate) =>
          val maxMonths    = determineMaxMonths(LocalDate.parse(startDate))
          val formProvider = WorkingFromHomeHoursFormProvider(request.userType, maxMonths)
          val formValues   = (
            request.getValue(WorkingFromHomeHours25To50, businessId),
            request.getValue(WorkingFromHomeHours51To100, businessId),
            request.getValue(WorkingFromHomeHours101Plus, businessId)
          )

          val filledForm = formValues match {
            case (Some(value25To50), Some(value51To100), Some(value101Plus)) =>
              formProvider.fill(WorkingFromHomeHoursFormModel(value25To50, value51To100, value101Plus))
            case _ => formProvider
          }

          Ok(view(filledForm, mode, request.userType, taxYear, businessId, maxMonths.toString))
      }
  }

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      service.getBusiness(request.nino, businessId, request.mtditid) map {
        case Left(_) => Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Right(business) =>
          business.head.commencementDate match {
            case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
            case Some(startDate) =>
              val maxMonths    = determineMaxMonths(LocalDate.parse(startDate))
              val formProvider = WorkingFromHomeHoursFormProvider(request.userType, maxMonths)
              val value25To50  = request.getValue(WorkingFromHomeHours25To50, businessId)
              val value51To100 = request.getValue(WorkingFromHomeHours51To100, businessId)
              val value101Plus = request.getValue(WorkingFromHomeHours101Plus, businessId)
              val filledForm = (value25To50, value51To100, value101Plus) match {
                case (Some(value25To50), Some(value51To100), Some(value101Plus)) =>
                  formProvider.fill(WorkingFromHomeHoursFormModel(value25To50, value51To100, value101Plus))
                case _ => formProvider
              }

              Ok(view(filledForm, mode, request.userType, taxYear, businessId, maxMonths.toString))
          }
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(form: WorkingFromHomeHoursFormModel): Future[Result] =
        for {
          firstUpdated  <- service.persistAnswer(businessId, request.userAnswers, form.value25To50, WorkingFromHomeHours25To50)
          secondUpdated <- service.persistAnswer(businessId, firstUpdated, form.value51To100, WorkingFromHomeHours51To100)
          result <- service
            .persistAnswer(businessId, secondUpdated, form.value101Plus, WorkingFromHomeHours101Plus)
            .map(updated => Redirect(navigator.nextPage(WorkingFromHomeHoursPage, mode, updated, taxYear, businessId)))
        } yield result

      service.getBusiness(request.nino, businessId, request.mtditid) flatMap {
        case Left(_) => Future(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Right(business) =>
          val startDate    = LocalDate.parse(business.head.commencementDate.get)
          val maxMonths    = determineMaxMonths(startDate)
          val formProvider = WorkingFromHomeHoursFormProvider(request.userType, maxMonths)
          formProvider
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, maxMonths.toString))),
              successfulForm => handleSuccess(successfulForm)
            )
      }
  }
  val bool = if (LocalDate.now().isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear

  private def determineMaxMonths(startDate: LocalDate): Int = {
    val defaultMaxMonths              = 12
    val startDateIsInTaxYear: Boolean = ChronoUnit.YEARS.between(startDate, taxYearCutoffDate) < 1
    if (startDateIsInTaxYear) {
      ChronoUnit.MONTHS.between(startDate, taxYearCutoffDate).toInt
    } else {
      defaultMaxMonths
    }
  }

}
