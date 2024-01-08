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
import controllers.standard.routes
import forms.income.AnyOtherIncomeFormProvider
import models.Mode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, TaxYear}
import navigation.IncomeNavigator
import pages.income.{AnyOtherIncomePage, OtherIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.AnyOtherIncomeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnyOtherIncomeController @Inject() (override val messagesApi: MessagesApi,
                                          selfEmploymentService: SelfEmploymentService,
                                          sessionRepository: SessionRepository,
                                          navigator: IncomeNavigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: AnyOtherIncomeFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: AnyOtherIncomeView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AnyOtherIncomePage, Some(businessId)) match {
        case None        => formProvider(request.userType)
        case Some(value) => formProvider(request.userType).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  // TODO simplify by using EitherT + for comprehension
  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_) => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) =>
          formProvider(request.userType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry {
                    val userAnswers =
                      if (!value) {
                        request.userAnswers.remove(OtherIncomeAmountPage, Some(businessId)).get
                      } else {
                        request.userAnswers
                      }
                    userAnswers.set(AnyOtherIncomePage, value, Some(businessId))
                  }
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(AnyOtherIncomePage, mode, updatedAnswers, taxYear, businessId, Some(accountingType.equals(Accrual.entryName)))
                )
            )
      }
  }

}
