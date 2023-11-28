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
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TradingAllowanceFormProvider
import models.Mode
import models.common.ModelUtils.userType
import models.common.{BusinessId, TaxYear}
import models.journeys.income.TradingAllowance.DeclareExpenses
import navigation.IncomeNavigator
import pages.income.{HowMuchTradingAllowancePage, TradingAllowanceAmountPage, TradingAllowancePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.TradingAllowanceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TradingAllowanceController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentService,
                                            sessionRepository: SessionRepository,
                                            navigator: IncomeNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: TradingAllowanceFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: TradingAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) map {
        case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
        case Right(accountingType) =>
          val preparedForm = request.userAnswers.get(TradingAllowancePage, Some(businessId)) match {
            case None        => formProvider(userType(request.user.isAgent))
            case Some(value) => formProvider(userType(request.user.isAgent)).fill(value)
          }

          Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId, accountingType))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) =>
          formProvider(userType(request.user.isAgent))
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId, accountingType))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry {
                    val userAnswers =
                      if (value.equals(DeclareExpenses)) {
                        request.userAnswers
                          .remove(HowMuchTradingAllowancePage, Some(businessId))
                          .get
                          .remove(TradingAllowanceAmountPage, Some(businessId))
                          .get
                      } else {
                        request.userAnswers
                      }
                    userAnswers.set(TradingAllowancePage, value, Some(businessId))
                  }
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TradingAllowancePage, mode, updatedAnswers, taxYear, businessId))
            )
      }
  }

}
