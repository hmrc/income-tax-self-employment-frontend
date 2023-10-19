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
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.income.TradingAllowancePage
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
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: TradingAllowanceFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: TradingAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def isAgentString(isAgent: Boolean): String = if (isAgent) "agent" else "individual"

  val businessId = "SJPR05893938418" // TODO merge 5840, delete default

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) async { // TODO add requireData SASS-5841
    implicit request =>
      val isAgent = isAgentString(request.user.isAgent)

      selfEmploymentService.getBusinessAccountingType(request.user.nino, businessId, request.user.mtditid) map {
        case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
        case Right(accountingType) =>
          val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(TradingAllowancePage) match {
            case None        => formProvider(isAgent)
            case Some(value) => formProvider(isAgent).fill(value)
          }

          Ok(view(preparedForm, mode, isAgent, taxYear, accountingType))
      }
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) async { // TODO add requireData SASS-5841
    implicit request =>
      selfEmploymentService.getBusinessAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) =>
          formProvider(isAgentString(request.user.isAgent))
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, isAgentString(request.user.isAgent), taxYear, accountingType))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(TradingAllowancePage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TradingAllowancePage, mode, updatedAnswers, taxYear))
            )
      }
  }

}
