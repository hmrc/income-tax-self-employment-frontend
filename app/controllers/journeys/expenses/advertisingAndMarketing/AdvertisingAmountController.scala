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

package controllers.journeys.expenses.advertisingAndMarketing

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.advertisingAndMarketing.AdvertisingAmountFormProvider
import models.Mode
import models.common.AccountingType.getAccountTypeFromString
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.advertisingAndMarketing.AdvertisingAndMarketingAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.advertisingAndMarketing.AdvertisingAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdvertisingAmountController @Inject() (override val messagesApi: MessagesApi,
                                             selfEmploymentService: SelfEmploymentService,
                                             sessionRepository: SessionRepository,
                                             navigator: ExpensesNavigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             formProvider: AdvertisingAmountFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: AdvertisingAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val user = request.userType
    val preparedForm =
      request.userAnswers.getOrElse(UserAnswers(request.userId)).get(AdvertisingAndMarketingAmountPage, Some(businessId)) match {
        case None        => formProvider(user)
        case Some(value) => formProvider(user).fill(value)
      }
    Ok(view(preparedForm, mode, user, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
      case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
      case Right(accountingType) =>
        val user = request.userType
        formProvider(user)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, user, taxYear, businessId))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).set(AdvertisingAndMarketingAmountPage, value, Some(businessId)))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator
                  .nextPage(AdvertisingAndMarketingAmountPage, mode, updatedAnswers, taxYear, businessId, getAccountTypeFromString(accountingType)))
          )
    }
  }

}
