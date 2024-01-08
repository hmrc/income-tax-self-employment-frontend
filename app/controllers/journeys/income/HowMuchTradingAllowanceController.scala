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
import forms.income.HowMuchTradingAllowanceFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.income.HowMuchTradingAllowance.Maximum
import navigation.IncomeNavigator
import pages.income.{HowMuchTradingAllowancePage, TradingAllowanceAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService.getIncomeTradingAllowance
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.income.HowMuchTradingAllowanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowMuchTradingAllowanceController @Inject() (override val messagesApi: MessagesApi,
                                                   sessionRepository: SessionRepository,
                                                   navigator: IncomeNavigator,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: HowMuchTradingAllowanceFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: HowMuchTradingAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val tradingAllowance       = getIncomeTradingAllowance(businessId, request.userAnswers)
      val tradingAllowanceString = formatMoney(tradingAllowance, addDecimalForWholeNumbers = false)
      val preparedForm = request.userAnswers.get(HowMuchTradingAllowancePage, Some(businessId)) match {
        case None        => formProvider(request.userType, tradingAllowanceString)
        case Some(value) => formProvider(request.userType, tradingAllowanceString).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId, tradingAllowanceString))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val tradingAllowance       = getIncomeTradingAllowance(businessId, request.userAnswers)
      val tradingAllowanceString = formatMoney(tradingAllowance, addDecimalForWholeNumbers = false)
      formProvider(request.userType, tradingAllowanceString)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, tradingAllowanceString))),
          value =>
            for {
              updatedAnswers <- Future.fromTry {
                val userAnswers =
                  if (value.equals(Maximum)) {
                    request.userAnswers.remove(TradingAllowanceAmountPage, Some(businessId)).get
                  } else {
                    request.userAnswers
                  }
                userAnswers.set(HowMuchTradingAllowancePage, value, Some(businessId))
              }
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(HowMuchTradingAllowancePage, mode, updatedAnswers, taxYear, businessId))
        )
  }

}
