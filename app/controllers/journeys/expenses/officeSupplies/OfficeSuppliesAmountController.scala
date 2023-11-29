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
import models.common.ModelUtils.userType
import models.common.{BusinessId, TaxYear}
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

  def onPageLoad(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, BusinessId(businessId), request.user.mtditid).map {
      case Right(accountingType) =>
        val preparedForm =
          request.userAnswers.getOrElse(UserAnswers(request.userId)).get(OfficeSuppliesAmountPage, Some(BusinessId(businessId))) match {
            case None        => formProvider(userType(request.user.isAgent))
            case Some(value) => formProvider(userType(request.user.isAgent)).fill(value)
          }

        Ok(view(preparedForm, mode, userType(request.user.isAgent), accountingType, taxYear, businessId))

      case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, BusinessId(businessId), request.user.mtditid).flatMap {
      case Right(accountingType) =>
        formProvider(userType(request.user.isAgent))
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), accountingType, taxYear, businessId))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).set(OfficeSuppliesAmountPage, value, Some(BusinessId(businessId))))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(OfficeSuppliesAmountPage, mode, updatedAnswers, taxYear, BusinessId(businessId)))
          )
      case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
    }

  }

}
