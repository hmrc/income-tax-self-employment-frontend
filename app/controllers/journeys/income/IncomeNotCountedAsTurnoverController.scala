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
import forms.income.IncomeNotCountedAsTurnoverFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import navigation.IncomeNavigator
import pages.income.{IncomeNotCountedAsTurnoverPage, NonTurnoverIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.IncomeNotCountedAsTurnoverView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeNotCountedAsTurnoverController @Inject() (override val messagesApi: MessagesApi,
                                                      sessionRepository: SessionRepository,
                                                      navigator: IncomeNavigator,
                                                      identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      formProvider: IncomeNotCountedAsTurnoverFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: IncomeNotCountedAsTurnoverView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(IncomeNotCountedAsTurnoverPage, Some(businessId)) match {
      case None        => formProvider(request.userType)
      case Some(value) => formProvider(request.userType).fill(value)
    }

    Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    formProvider(request.userType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
        value =>
          for {
            updatedAnswers <- Future.fromTry {
              val userAnswers =
                if (!value) {
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).remove(NonTurnoverIncomeAmountPage, Some(businessId)).get
                } else {
                  request.userAnswers.getOrElse(UserAnswers(request.userId))
                }
              userAnswers.set(IncomeNotCountedAsTurnoverPage, value, Some(businessId))
            }
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IncomeNotCountedAsTurnoverPage, mode, updatedAnswers, taxYear, businessId))
      )
  }

}
