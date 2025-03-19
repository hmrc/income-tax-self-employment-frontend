/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.common.{BusinessId, TaxYear}
import forms.expenses.travelAndAccommodation.TravelAndAccommodationFormProvider
import models.Mode
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import navigation.ExpensesTailoringNavigator
import pages.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TravelAndAccommodationExpenseTypeController @Inject() (override val messagesApi: MessagesApi,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             service: SelfEmploymentService,
                                                             identify: IdentifierAction,
                                                             getData: DataRetrievalAction,
                                                             requireData: DataRequiredAction,
                                                             formProvider: TravelAndAccommodationFormProvider,
                                                             sessionRepository: SessionRepository,
                                                             navigator: ExpensesTailoringNavigator,
                                                             view: TravelAndAccommodationExpenseTypeView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = TravelAndAccommodationExpenseTypePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[Set[TravelAndAccommodationExpenseType]] = formProvider(request.user.userType)

      val filledForm = request.userAnswers.get(page) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[Set[TravelAndAccommodationExpenseType]] = formProvider(request.user.userType)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId))
        )
    }

}
