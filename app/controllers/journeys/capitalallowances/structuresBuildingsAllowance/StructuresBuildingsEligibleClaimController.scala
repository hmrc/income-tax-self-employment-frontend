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

import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsEligibleClaimFormProvider
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsEligibleClaimPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsEligibleClaimView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class StructuresBuildingsEligibleClaimController @Inject() (override val messagesApi: MessagesApi,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: StructuresBuildingsEligibleClaimFormProvider,
                                                            service: StructuresBuildingsService,
                                                            view: StructuresBuildingsEligibleClaimView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form = request.userAnswers
      .get(StructuresBuildingsEligibleClaimPage, businessId.some)
      .fold(formProvider(request.userType))(formProvider(request.userType).fill)

    Ok(view(form, NormalMode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, NormalMode, request.userType, taxYear, businessId))),
          answer => service.submitAnswerAndRedirect(StructuresBuildingsEligibleClaimPage, businessId, request, answer, taxYear, NormalMode)
        )
  }

}
