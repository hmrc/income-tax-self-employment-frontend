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
import forms.standard.LocalDateFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsQualifyingUseDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Logging, TimeMachine}
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsQualifyingUseDateView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class StructuresBuildingsQualifyingUseDateController @Inject() (override val messagesApi: MessagesApi,
                                                                identify: IdentifierAction,
                                                                getData: DataRetrievalAction,
                                                                requireData: DataRequiredAction,
                                                                service: StructuresBuildingsService,
                                                                formProvider: LocalDateFormProvider,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                view: StructuresBuildingsQualifyingUseDateView,
                                                                timeMachine: TimeMachine)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page               = StructuresBuildingsQualifyingUseDatePage
  private val latestDateAndError = Some((timeMachine.now, "structuresBuildingsQualifyingUseDate.error.inFuture"))
  private val form = (userType: UserType) => formProvider(page, userType, userSpecificRequiredError = true, latestDateAndError = latestDateAndError)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val filledForm = page.fillFormWithIndex(form(request.userType), page, request, businessId, index)
      Ok(view(filledForm, mode, request.userType, taxYear, businessId, index))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) async { implicit request =>
      form(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId, index))),
          answer => service.updateAndRedirectWithIndex(request.userAnswers, answer, businessId, taxYear, index, page)
        )
    }

}
