/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.income.AnyOtherIncomeFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import navigation.IncomeNavigator
import pages.income.{AnyOtherIncomePage, OtherIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.AnyOtherIncomeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AnyOtherIncomeController @Inject() (override val messagesApi: MessagesApi,
                                          navigator: IncomeNavigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          service: SelfEmploymentService,
                                          formProvider: AnyOtherIncomeFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: AnyOtherIncomeView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers
        .get(AnyOtherIncomePage, Some(businessId))
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(accountingType: AccountingType): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
            anyOtherIncomeValue => handleSuccess(anyOtherIncomeValue, accountingType)
          )

      def handleSuccess(anyOtherIncomeValue: Boolean, accountingType: AccountingType): Future[Result] = {
        val adjustedAnswers =
          if (anyOtherIncomeValue) request.userAnswers.pure[Try] else request.userAnswers.remove(OtherIncomeAmountPage, Some(businessId))
        for {
          answers        <- Future.fromTry(adjustedAnswers)
          updatedAnswers <- service.persistAnswer(businessId, answers, anyOtherIncomeValue, AnyOtherIncomePage)
        } yield Redirect(navigator.nextPage(AnyOtherIncomePage, mode, updatedAnswers, taxYear, businessId, Some(accountingType)))
      }

      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        result         <- EitherT.right[Result](handleForm(accountingType))
      } yield result).merge

  }

}
