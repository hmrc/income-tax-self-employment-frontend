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

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.{handleResultT, redirectJourneyRecovery}
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider
import forms.expenses.workplaceRunningCosts.workingFromHome.WorkingFromHomeHoursFormProvider.WorkingFromHomeHoursFormModel
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.domain.BusinessData
import models.errors.ServiceError
import models.requests.DataRequest
import navigation.ExpensesNavigator
import pages.expenses.workplaceRunningCosts.workingFromHome._
import play.api.data.Form
import play.api.i18n._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
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
    with I18nSupport
    with Logging {

  private val defaultMaxMonths = 12

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val result = service.getBusiness(request.nino, businessId, request.mtditid) map { business =>
        getFilledFormAndMaxMonths(request, business, businessId) match {
          case Left(redirectError) => redirectError
          case Right((filledForm: Form[WorkingFromHomeHoursFormModel], maxMonths: Int)) =>
            Ok(view(filledForm, mode, request.userType, taxYear, businessId, maxMonths.toString))
        }
      }
      handleResultT(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(maxMonths: Int): Future[Result] = {
        val formProvider = WorkingFromHomeHoursFormProvider(request.userType, maxMonths)
        formProvider
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, maxMonths.toString))),
            successfulForm => handleSuccess(successfulForm)
          )
      }
      def handleSuccess(form: WorkingFromHomeHoursFormModel): Future[Result] =
        for {
          firstUpdated  <- service.persistAnswer(businessId, request.userAnswers, form.value25To50, WorkingFromHomeHours25To50)
          secondUpdated <- service.persistAnswer(businessId, firstUpdated, form.value51To100, WorkingFromHomeHours51To100)
          thirdUpdated  <- service.persistAnswer(businessId, secondUpdated, form.value101Plus, WorkingFromHomeHours101Plus)
          result = Redirect(navigator.nextPage(WorkingFromHomeHoursPage, mode, thirdUpdated, taxYear, businessId))
        } yield result

      val result: EitherT[Future, ServiceError, Result] =
        service.getBusiness(request.nino, businessId, request.mtditid) flatMap (business =>
          getMaxMonthsOrRedirect(business) match {
            case Left(redirect: Result) => EitherT.right[ServiceError](Future.successful(redirect))
            case Right(maxMonths: Int)  => EitherT.right[ServiceError](handleForm(maxMonths))
          })

      handleResultT(result)
  }

  private def getFilledFormAndMaxMonths(request: DataRequest[_], business: BusinessData, businessId: BusinessId)(implicit
      messages: Messages): Either[Result, (Form[WorkingFromHomeHoursFormModel], Int)] =
    getMaxMonthsOrRedirect(business) match {
      case Left(redirect) => Left(redirect)
      case Right(maxMonths) =>
        val formProvider = WorkingFromHomeHoursFormProvider(request.userType, maxMonths)
        val value25To50  = request.getValue(WorkingFromHomeHours25To50, businessId)
        val value51To100 = request.getValue(WorkingFromHomeHours51To100, businessId)
        val value101Plus = request.getValue(WorkingFromHomeHours101Plus, businessId)
        val filledForm: Form[WorkingFromHomeHoursFormModel] = (value25To50, value51To100, value101Plus) match {
          case (Some(value25To50), Some(value51To100), Some(value101Plus)) =>
            formProvider.fill(WorkingFromHomeHoursFormModel(value25To50, value51To100, value101Plus))
          case _ => formProvider
        }
        Right((filledForm, maxMonths))
    }

  private def getMaxMonthsOrRedirect(business: BusinessData): Either[Result, Int] =
    business.commencementDate.fold[Either[Result, Int]] {
      Left(redirectJourneyRecovery())
    } { date =>
      val startDate: LocalDate          = LocalDate.parse(date)
      val startDateIsInTaxYear: Boolean = ChronoUnit.YEARS.between(startDate, taxYearCutoffDate) < 1
      if (startDateIsInTaxYear) {
        Right(ChronoUnit.MONTHS.between(startDate, taxYearCutoffDate).toInt)
      } else {
        Right(defaultMaxMonths)
      }
    }

}