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
import controllers.actions._
import controllers.handleServiceCall
import forms.income.OtherIncomeAmountFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import navigation.IncomeNavigator
import pages.income.OtherIncomeAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.OtherIncomeAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherIncomeAmountController @Inject() (override val messagesApi: MessagesApi,
                                             navigator: IncomeNavigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             service: SelfEmploymentService,
                                             formProvider: OtherIncomeAmountFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: OtherIncomeAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers
        .get(OtherIncomeAmountPage, Some(businessId))
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
            value =>
              service
                .persistAnswer(businessId, request.userAnswers, value, OtherIncomeAmountPage)
                .map(updatedAnswers =>
                  Redirect(navigator.nextPage(OtherIncomeAmountPage, mode, updatedAnswers, taxYear, businessId, Some(accountingType))))
          )

      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.nino, businessId, request.mtditid))
        result         <- EitherT.right[Result](handleForm(accountingType))
      } yield result).merge
  }

}
