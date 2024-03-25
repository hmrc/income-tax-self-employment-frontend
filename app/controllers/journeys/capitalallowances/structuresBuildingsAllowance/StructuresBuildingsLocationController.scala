/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys.fillForm
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocationFormProvider
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocationFormProvider.filterErrors
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocationView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class StructuresBuildingsLocationController @Inject() (override val messagesApi: MessagesApi,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       service: SelfEmploymentService,
                                                       formProvider: StructuresBuildingsLocationFormProvider,
                                                       view: StructuresBuildingsLocationView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = StructuresBuildingsLocationPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val filledForm = page.fillFormWithIndex(formProvider(request.userType), page, request, businessId, index)
      Ok(view(filledForm, mode, request.userType, taxYear, businessId, index))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) async { implicit request =>
      val form = formProvider(request.userType)
      form
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(filterErrors(formErrors, request.userType), mode, request.userType, taxYear, businessId))),
          answer => service.persistAnswerAndRedirect(page, businessId, request, answer, taxYear, mode)
        )
    }

}
