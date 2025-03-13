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
import models.common.{BusinessId, TaxYear, UserType}
import controllers.journeys.fillForm
import forms.expenses.travelAndAccommodation.TravelAndAccommodationFormProvider
import models.Mode
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import pages.expenses.travelAndAccommodation.TravelAndAccommodationExpenseTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}

@Singleton
class TravelAndAccommodationExpenseTypeController @Inject() (override val messagesApi: MessagesApi,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         service: SelfEmploymentService,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: TravelAndAccommodationFormProvider,
                                                         view: TravelAndAccommodationExpenseTypeView)
  extends FrontendBaseController
  with I18nSupport {

  private val page = TravelAndAccommodationExpenseTypePage
  private val form = (userType: UserType) => formProvider(userType)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = request.userAnswers.get(page) match {
        case None        => form
        case Some(value) => fillForm(value)
      }

      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleFormError(formWithErrors: Form[_]): Result =
        BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))

      service.defaultHandleForm(form(request.userType), page, businessId, taxYear, mode, handleFormError)
  }

}
