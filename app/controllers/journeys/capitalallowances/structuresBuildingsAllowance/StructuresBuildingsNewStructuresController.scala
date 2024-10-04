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
import forms.standard.BooleanFormProvider
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructuresBuildingsAnswers.removeIncompleteStructure
import pages.capitalallowances.structuresBuildingsAllowance.{NewStructuresBuildingsList, StructuresBuildingsNewStructuresPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.journeys.capitalallowances.structuresBuildingsAllowance.NewStructuresBuildingsViewModel.getNewStructuresSummaryRows
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsNewStructuresView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StructuresBuildingsNewStructuresController @Inject() (override val messagesApi: MessagesApi,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: BooleanFormProvider,
                                                            service: SelfEmploymentService,
                                                            view: StructuresBuildingsNewStructuresView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val page = StructuresBuildingsNewStructuresPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request
        .valueOrFutureRedirectDefault(NewStructuresBuildingsList, businessId)
        .map { sites =>
          val cleanSiteList = removeIncompleteStructure(sites)
          service.persistAnswer(businessId, request.userAnswers, cleanSiteList, NewStructuresBuildingsList).map { _ =>
            val summaryList = getNewStructuresSummaryRows(cleanSiteList, taxYear, businessId)
            Ok(view(formProvider(page, request.userType), request.userType, taxYear, businessId, summaryList))
          }
        }
        .merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request
      .valueOrRedirectDefault(NewStructuresBuildingsList, businessId)
      .map { structures =>
        val summaryList = getNewStructuresSummaryRows(structures, taxYear, businessId)
        formProvider(page, request.userType)
          .bindFromRequest()
          .fold(
            formErrors => BadRequest(view(formErrors, request.userType, taxYear, businessId, summaryList)),
            answer =>
              Redirect(
                if (answer) routes.StructuresBuildingsQualifyingUseDateController.onPageLoad(taxYear, businessId, structures.length, NormalMode)
                else routes.StructuresBuildingsPreviousClaimUseController.onPageLoad(taxYear, businessId, NormalMode)
              )
          )
      }
      .merge
  }

}
