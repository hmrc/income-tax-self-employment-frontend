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
import controllers.journeys.{clearDependentPages, fillForm}
import forms.standard.BooleanFormProvider
import models.common.Journey.ExpensesStaffCosts
import models.common.{BusinessId, Journey, TaxYear}
import models.{CheckMode, Mode}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.DisallowableStaffCostsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.DisallowableStaffCostsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisallowableStaffCostsController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: ExpensesTailoringNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  hopChecker: HopCheckerAction,
                                                  formProvider: BooleanFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: DisallowableStaffCostsView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val page = DisallowableStaffCostsPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      val form = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(view(form, mode, request.userType, taxYear, businessId))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <-
                if (mode == CheckMode && !request.userAnswers.get(DisallowableStaffCostsPage, businessId).contains(value)) {
                  selfEmploymentService.clearExpensesData(taxYear, businessId, ExpensesStaffCosts)
                  clearDependentPages(DisallowableStaffCostsPage, value, request.userAnswers, businessId)
                } else {
                  Future.successful(request.userAnswers)
                }
              savedAnswers <- selfEmploymentService.persistAnswer(businessId, updatedAnswers, value, DisallowableStaffCostsPage)
            } yield Redirect(navigator.nextPage(DisallowableStaffCostsPage, mode, savedAnswers, taxYear, businessId))
        )
  }

}
