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

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.returnAccountingType
import forms.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepairsAndMaintenanceAmountController @Inject() (override val messagesApi: MessagesApi,
                                                       selfEmploymentService: SelfEmploymentService,
                                                       navigator: ExpensesNavigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: RepairsAndMaintenanceAmountFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: RepairsAndMaintenanceAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = request.userAnswers
        .get(RepairsAndMaintenanceAmountPage, businessId.some)
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(form, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def handleSuccess(value: BigDecimal): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, request.userAnswers, value, RepairsAndMaintenanceAmountPage)
          .map(updated => Redirect(navigator.nextPage(RepairsAndMaintenanceAmountPage, mode, updated, taxYear, businessId)))

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))),
          value => handleSuccess(value)
        )
  }

}
