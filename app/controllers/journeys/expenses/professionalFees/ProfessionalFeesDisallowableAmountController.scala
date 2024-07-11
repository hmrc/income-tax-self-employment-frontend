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

package controllers.journeys.expenses.professionalFees

import cats.data.EitherT
import controllers.actions._
import forms.expenses.professionalFees.ProfessionalFeesDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.professionalFees.ProfessionalFeesDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfessionalFeesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                              selfEmploymentService: SelfEmploymentService,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalAction,
                                                              requireData: DataRequiredAction,
                                                              formProvider: ProfessionalFeesDisallowableAmountFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: ProfessionalFeesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  private val page = ProfessionalFeesDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(ProfessionalFeesAmountPage, businessId)
        existingAnswer = request.getValue(page, businessId)
        form           = formProvider(request.userType, allowableAmount)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleFormError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))

      def handleForm(form: Form[BigDecimal], allowableAmount: BigDecimal): Future[Result] =
        selfEmploymentService.defaultHandleForm(form, page, businessId, taxYear, mode, handleFormError(allowableAmount))

      (for {
        allowableAmount <- EitherT.fromEither[Future](request.valueOrRedirectDefault(ProfessionalFeesAmountPage, businessId))
        form = formProvider(request.userType, allowableAmount)
        finalResult <- EitherT.right[Result](handleForm(form, allowableAmount))
      } yield finalResult).merge
  }
}
