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
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.income.{TradingAllowanceAmountPage, TurnoverIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.TradingAllowanceAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TradingAllowanceAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  sessionRepository: SessionRepository,
                                                  navigator: Navigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: TradingAllowanceAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: TradingAllowanceAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def authUserType(isAgent: Boolean) = if (isAgent) "agent" else "individual"

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) { // TODO add requireData SASS-5841
    implicit request =>
      val turnoverAmount: BigDecimal = {
        val turnover: BigDecimal = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(TurnoverIncomeAmountPage).getOrElse(1000.00)
        if (turnover > 1000.00) 1000.00 else turnover
      }
      val isAgent = authUserType(request.user.isAgent)
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(TradingAllowanceAmountPage) match {
        case None        => formProvider(isAgent, turnoverAmount)
        case Some(value) => formProvider(isAgent, turnoverAmount).fill(value)
      }

      Ok(view(preparedForm, mode, isAgent, taxYear))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) async { // TODO add requireData SASS-5841
    implicit request =>
      val turnoverAmount: BigDecimal = {
        val turnover: BigDecimal = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(TurnoverIncomeAmountPage).getOrElse(1000.00)
        if (turnover > 1000.00) 1000.00 else turnover
      }
      formProvider(authUserType(request.user.isAgent), turnoverAmount)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, authUserType(request.user.isAgent), taxYear))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(TradingAllowanceAmountPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TradingAllowanceAmountPage, mode, updatedAnswers, taxYear))
        )
  }

}
