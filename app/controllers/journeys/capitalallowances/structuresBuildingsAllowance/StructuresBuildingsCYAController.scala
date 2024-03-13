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
import controllers.journeys.capitalallowances.structuresBuildingsAllowance
import controllers.journeys
import models.NormalMode
import models.common._
import models.journeys.Journey.CapitalAllowancesStructuresBuildings
import pages.capitalallowances.tailoring.CapitalAllowancesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.capitalallowances.structuresBuildingsAllowance.{
  StructuresBuildingsAllowanceSummary,
  StructuresBuildingsClaimedSummary,
  StructuresBuildingsPreviousClaimUseSummary
}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}

@Singleton
class StructuresBuildingsCYAController @Inject() (override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getAnswers: DataRetrievalAction,
                                                  requireAnswers: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getAnswers andThen requireAnswers) { implicit request =>
      val summaryList =
        SummaryListCYA.summaryListOpt(
          List(
            StructuresBuildingsAllowanceSummary.row(request.userAnswers, taxYear, businessId, request.userType),
            StructuresBuildingsClaimedSummary.row(request.userAnswers, taxYear, businessId, request.userType),
            StructuresBuildingsPreviousClaimUseSummary.row(request.userAnswers, taxYear, businessId, request.userType)
          )
        )

      Ok(
        view(
          CapitalAllowancesCYAPage,
          taxYear,
          request.userType,
          summaryList,
          structuresBuildingsAllowance.routes.StructuresBuildingsCYAController.onSubmit(taxYear, businessId)
        ))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) { _ =>
    Redirect(
      journeys.routes.SectionCompletedStateController
        .onPageLoad(taxYear, businessId, CapitalAllowancesStructuresBuildings.entryName, NormalMode))
  }

}
