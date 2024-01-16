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

package controllers.journeys.capitalallowances.tailoring

import cats.data.EitherT
import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.handleServiceCall
import forms.capitalallowances.tailoring.SelectCapitalAllowancesFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.tailoring.SelectCapitalAllowancesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.tailoring.SelectCapitalAllowancesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelectCapitalAllowancesController @Inject() (override val messagesApi: MessagesApi,
                                                   navigator: CapitalAllowancesNavigator,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   service: SelfEmploymentService,
                                                   formProvider: SelectCapitalAllowancesFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: SelectCapitalAllowancesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        form = request.userAnswers
          .get(SelectCapitalAllowancesPage, businessId.some)
          .fold(formProvider(request.userType))(formProvider(request.userType).fill)
      } yield Ok(view(form, mode, request.userType, taxYear, businessId, accountingType))).merge

  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(accountingType: AccountingType): Future[Result] =
        formProvider(request.userType)
          .bindFromRequest()
          .fold(
            formErrors => {
              println("%%" + formErrors)
              Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId, accountingType)))
            },
            value =>
              service
                .persistAnswer(businessId, request.userAnswers, value, SelectCapitalAllowancesPage)
                .map(updatedAnswers => Redirect(navigator.nextPage(SelectCapitalAllowancesPage, mode, updatedAnswers, taxYear, businessId)))
          )

      (for {
        accountingType <- handleServiceCall(service.getAccountingType(request.user.nino, businessId, request.user.mtditid))
        result         <- EitherT.right[Result](handleForm(accountingType))
      } yield result).merge

  }

}
