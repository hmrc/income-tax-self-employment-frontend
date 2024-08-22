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
import forms.expenses.tailoring.individualCategories.ProfessionalServiceExpensesFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.common.Journey
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses.{Construction, ProfessionalFees, Staff}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.{
  DisallowableProfessionalFeesPage,
  DisallowableStaffCostsPage,
  DisallowableSubcontractorCostsPage,
  ProfessionalServiceExpensesPage
}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Settable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.tailoring.individualCategories.ProfessionalServiceExpensesView

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ProfessionalServiceExpensesController @Inject() (override val messagesApi: MessagesApi,
                                                       selfEmploymentService: SelfEmploymentService,
                                                       navigator: ExpensesTailoringNavigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       hopChecker: HopCheckerAction,
                                                       formProvider: ProfessionalServiceExpensesFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ProfessionalServiceExpensesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {
  private val page = ProfessionalServiceExpensesPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen
      hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      val form = fillForm(page, businessId, formProvider(request.userType))
      Ok(view(form, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(expenses: Set[ProfessionalServiceExpenses]): Future[Result] =
        for {
          clearedAnswers <- Future.fromTry(clearDependentPageAnswers(request.userAnswers, Some(businessId), expenses))
          updatedAnswers <- selfEmploymentService.persistAnswer(businessId, clearedAnswers, expenses, ProfessionalServiceExpensesPage)
        } yield Redirect(navigator.nextPage(ProfessionalServiceExpensesPage, mode, updatedAnswers, taxYear, businessId))

      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))),
          value => handleSuccess(value)
        )
  }

  private def clearDependentPageAnswers(userAnswers: UserAnswers,
                                        businessId: Option[BusinessId],
                                        pageAnswers: Set[ProfessionalServiceExpenses]): Try[UserAnswers] = {
    @nowarn("msg=match may not be exhaustive")
    def getPageFromAnswer(value: ProfessionalServiceExpenses): Settable[_] = value match {
      case Staff            => DisallowableStaffCostsPage: Settable[Boolean]
      case Construction     => DisallowableSubcontractorCostsPage: Settable[Boolean]
      case ProfessionalFees => DisallowableProfessionalFeesPage: Settable[Boolean]
    }
    val uncheckedAnswers: List[ProfessionalServiceExpenses] = List(Staff, Construction, ProfessionalFees).filterNot(pageAnswers.contains(_))
    val pagesToBeRemoved: List[Settable[_]]                 = uncheckedAnswers.map(getPageFromAnswer)

    clearDataFromUserAnswers(userAnswers, pagesToBeRemoved, businessId)
  }

}
