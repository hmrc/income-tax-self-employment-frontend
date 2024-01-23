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

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeId
import controllers.actions._
import controllers.handleServiceCall
import forms.income.TradingAllowanceFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import models.journeys.income.TradingAllowance
import models.journeys.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import navigation.IncomeNavigator
import pages.income.{HowMuchTradingAllowancePage, TradingAllowanceAmountPage, TradingAllowancePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.TradingAllowanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class TradingAllowanceController @Inject() (override val messagesApi: MessagesApi,
                                            navigator: IncomeNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: TradingAllowanceFormProvider,
                                            service: SelfEmploymentService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: TradingAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.nino, businessId, request.mtditid))
        form = request.userAnswers
          .get(TradingAllowancePage, Some(businessId))
          .fold(formProvider(request.userType))(formProvider(request.userType).fill)
      } yield Ok(view(form, mode, request.userType, taxYear, businessId, accountingType))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(accountingType: AccountingType): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId, accountingType))),
            value => handleSuccess(value)
          )

      def handleSuccess(value: TradingAllowance): Future[Result] = {
        val adjustedAnswers = value match {
          case DeclareExpenses =>
            request.userAnswers
              .remove(HowMuchTradingAllowancePage, Some(businessId))
              .flatMap(_.remove(TradingAllowanceAmountPage, Some(businessId)))

          case UseTradingAllowance =>
            request.userAnswers.pure[Try]
        }
        for {
          answers        <- Future.fromTry(adjustedAnswers)
          updatedAnswers <- service.persistAnswer(businessId, answers, value, TradingAllowancePage)
        } yield Redirect(navigator.nextPage(TradingAllowancePage, mode, updatedAnswers, taxYear, businessId))
      }

      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.nino, businessId, request.mtditid))
        result         <- EitherT.right[Result](handleForm(accountingType))
      } yield result).merge

  }

}
