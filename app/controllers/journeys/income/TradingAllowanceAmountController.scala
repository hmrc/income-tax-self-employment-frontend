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
import forms.income.TradingAllowanceAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.IncomeNavigator
import pages.income.TradingAllowanceAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService.getIncomeTradingAllowance
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.TradingAllowanceAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradingAllowanceAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  sessionRepository: SessionRepository,
                                                  navigator: IncomeNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: TradingAllowanceAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: TradingAllowanceAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val tradingAllowance: BigDecimal = getIncomeTradingAllowance(businessId, request.userAnswers)
      val preparedForm = request.userAnswers.get(TradingAllowanceAmountPage, Some(businessId)) match {
        case None        => formProvider(request.userType, tradingAllowance)
        case Some(value) => formProvider(request.userType, tradingAllowance).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val tradingAllowance: BigDecimal = getIncomeTradingAllowance(businessId, request.userAnswers)
      formProvider(request.userType, tradingAllowance)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TradingAllowanceAmountPage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TradingAllowanceAmountPage, mode, updatedAnswers, taxYear, businessId))
        )
  }

}
