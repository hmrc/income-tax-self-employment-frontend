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
import controllers.journeys.fillForm
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.IncomeNavigator
import pages.income.{NotTaxableAmountPage, TurnoverNotTaxablePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.TurnoverNotTaxableView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class TurnoverNotTaxableController @Inject() (override val messagesApi: MessagesApi,
                                              navigator: IncomeNavigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: BooleanFormProvider,
                                              service: SelfEmploymentService,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: TurnoverNotTaxableView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TurnoverNotTaxablePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(value: Boolean): Future[Result] = {
        val adjustedAnswers =
          if (value) request.userAnswers.pure[Try] else request.userAnswers.remove(NotTaxableAmountPage, Some(businessId))
        for {
          answers        <- Future.fromTry(adjustedAnswers)
          updatedAnswers <- service.persistAnswer(businessId, answers, value, page)
        } yield Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId))
      }

      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          value => handleSuccess(value)
        )
  }

}
