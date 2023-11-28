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

package controllers.journeys.expenses.repairsandmaintenance

import cats.data.EitherT
import controllers.actions._
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, TextAmount, UserType}
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepairsAndMaintenanceDisallowableAmountController @Inject() (
    override val messagesApi: MessagesApi,
    selfEmploymentService: SelfEmploymentServiceBase,
    navigator: ExpensesNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: RepairsAndMaintenanceDisallowableAmountFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: RepairsAndMaintenanceDisallowableAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(RepairsAndMaintenanceAmountPage, businessId)
        existingAnswer = request.getValue(RepairsAndMaintenanceDisallowableAmountPage, businessId)
        form           = formProvider(request.userType, allowableAmount)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, taxYear, businessId, request.userType, TextAmount(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(formWithErrors: Form[_], userType: UserType, allowableAmount: BigDecimal) =
        Future.successful(
          BadRequest(view(formWithErrors, mode, taxYear, businessId, userType, TextAmount(allowableAmount)))
        )

      def handleSuccess(userAnswers: UserAnswers, value: BigDecimal) =
        selfEmploymentService
          .saveAnswer(businessId, userAnswers, value, RepairsAndMaintenanceDisallowableAmountPage)
          .map(updated => Redirect(navigator.nextPage(RepairsAndMaintenanceDisallowableAmountPage, mode, updated, taxYear, businessId)))

      def handleForm(form: Form[BigDecimal], userType: UserType, userAnswers: UserAnswers, allowableAmount: BigDecimal): Future[Result] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors => handleError(formWithErrors, userType, allowableAmount),
            value => handleSuccess(userAnswers, value)
          )

      val obtainedAllowableValue = request.valueOrRedirectDefault(RepairsAndMaintenanceAmountPage, businessId)
      val result = for {
        allowableAmount <- EitherT.fromEither[Future](obtainedAllowableValue)
        userType    = request.userType
        userAnswers = request.userAnswers
        form        = formProvider(userType, allowableAmount)
        finalResult <- EitherT.right[Result](handleForm(form, userType, userAnswers, allowableAmount))
      } yield finalResult

      result.merge
  }

}
