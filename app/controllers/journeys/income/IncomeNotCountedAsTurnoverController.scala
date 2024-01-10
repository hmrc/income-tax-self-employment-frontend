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

import cats.implicits.catsSyntaxApplicativeId
import controllers.actions._
import forms.income.IncomeNotCountedAsTurnoverFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import navigation.IncomeNavigator
import pages.income.{IncomeNotCountedAsTurnoverPage, NonTurnoverIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.IncomeNotCountedAsTurnoverView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class IncomeNotCountedAsTurnoverController @Inject() (override val messagesApi: MessagesApi,
                                                      navigator: IncomeNavigator,
                                                      identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      formProvider: IncomeNotCountedAsTurnoverFormProvider,
                                                      service: SelfEmploymentService,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: IncomeNotCountedAsTurnoverView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  // TODO Add requireData action during SASS-5878 (see comment attached to this ticket).
  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers
      .getOrElse(UserAnswers(request.userId))
      .get(IncomeNotCountedAsTurnoverPage, Some(businessId))
      .fold(formProvider(request.userType))(formProvider(request.userType).fill)

    Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    def handleSuccess(value: Boolean): Future[Result] = {
      val adjustedAnswers =
        if (value) {
          request.userAnswers.getOrElse(UserAnswers(request.userId)).pure[Try]
        } else {
          request.userAnswers.getOrElse(UserAnswers(request.userId)).remove(NonTurnoverIncomeAmountPage, Some(businessId))
        }
      for {
        answers        <- Future.fromTry(adjustedAnswers)
        updatedAnswers <- service.persistAnswer(businessId, answers, value, IncomeNotCountedAsTurnoverPage)
      } yield Redirect(navigator.nextPage(IncomeNotCountedAsTurnoverPage, mode, updatedAnswers, taxYear, businessId))
    }

    formProvider(request.userType)
      .bindFromRequest()
      .fold(
        formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
        value => handleSuccess(value)
      )
  }

}
