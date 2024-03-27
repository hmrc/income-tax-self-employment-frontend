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

package controllers.journeys.expenses.tailoring.individualCategories

import controllers.actions._
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.expenses.tailoring.individualCategories.RepairsAndMaintenanceFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey
import models.journeys.expenses.individualCategories.RepairsAndMaintenance
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.RepairsAndMaintenancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.tailoring.individualCategories.RepairsAndMaintenanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepairsAndMaintenanceController @Inject() (override val messagesApi: MessagesApi,
                                                 selfEmploymentService: SelfEmploymentService,
                                                 navigator: ExpensesTailoringNavigator,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 hopChecker: HopCheckerAction,
                                                 formProvider: RepairsAndMaintenanceFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: RepairsAndMaintenanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {
  private val page = RepairsAndMaintenancePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      val form = fillForm(page, businessId, formProvider(request.userType))
      Ok(view(form, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(value: RepairsAndMaintenance): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, request.userAnswers, value, RepairsAndMaintenancePage)
          .map(updated => Redirect(navigator.nextPage(RepairsAndMaintenancePage, mode, updated, taxYear, businessId)))

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))),
          value => handleSuccess(value)
        )
  }

}
