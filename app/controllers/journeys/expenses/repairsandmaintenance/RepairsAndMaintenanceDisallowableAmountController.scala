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
import controllers.journeys.fillForm
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, TextAmount}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepairsAndMaintenanceDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                                   service: SelfEmploymentService,
                                                                   identify: IdentifierAction,
                                                                   getData: DataRetrievalAction,
                                                                   requireData: DataRequiredAction,
                                                                   formProvider: RepairsAndMaintenanceDisallowableAmountFormProvider,
                                                                   val controllerComponents: MessagesControllerComponents,
                                                                   view: RepairsAndMaintenanceDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = RepairsAndMaintenanceDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(RepairsAndMaintenanceAmountPage, businessId)
        form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
      } yield Ok(view(form, mode, taxYear, businessId, request.userType, TextAmount(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleError(allowableAmount: BigDecimal)(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, taxYear, businessId, request.userType, TextAmount(allowableAmount)))

      (for {
        allowableAmount <- EitherT.fromEither[Future](request.valueOrRedirectDefault(RepairsAndMaintenanceAmountPage, businessId))
        finalResult <- EitherT.right[Result](
          service.defaultHandleForm(formProvider(request.userType, allowableAmount), page, businessId, taxYear, mode, handleError(allowableAmount)))
      } yield finalResult).merge
  }

}
