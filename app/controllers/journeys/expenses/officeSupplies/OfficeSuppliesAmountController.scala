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

package controllers.journeys.expenses.officeSupplies

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.officeSupplies.OfficeSuppliesAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.officeSupplies.OfficeSuppliesAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeSuppliesAmountController @Inject() (override val messagesApi: MessagesApi,
                                                sessionRepository: SessionRepository,
                                                selfEmploymentService: SelfEmploymentService,
                                                navigator: ExpensesNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                formProvider: OfficeSuppliesAmountFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: OfficeSuppliesAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid).map {
      case Right(accountingType) =>
        val preparedForm =
          request.userAnswers.getOrElse(UserAnswers(request.userId)).get(OfficeSuppliesAmountPage, Some(businessId)) match {
            case None        => formProvider(request.userType)
            case Some(value) => formProvider(request.userType).fill(value)
          }

        Ok(view(preparedForm, mode, request.userType, AccountingType.withName(accountingType), taxYear, businessId))

      case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid).flatMap {
      case Right(accountingType) =>
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, AccountingType.withName(accountingType), taxYear, businessId))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).set(OfficeSuppliesAmountPage, value, Some(businessId)))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(OfficeSuppliesAmountPage, mode, updatedAnswers, taxYear, businessId))
          )
      case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
    }

  }

}
