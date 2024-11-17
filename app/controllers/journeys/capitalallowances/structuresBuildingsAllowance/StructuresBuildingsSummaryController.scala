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
import controllers.redirectJourneyRecovery
import models.common._
import pages.Page
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsAllowancePage.getStructureFromIndex
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.capitalallowances.structuresBuildingsAllowance.{
  StructureBuildingLocationSummary,
  StructuresBuildingsNewClaimAmountSummary,
  StructuresBuildingsQualifyingExpenditureSummary,
  StructuresBuildingsUseDateSummary
}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}

@Singleton
class StructuresBuildingsSummaryController @Inject() (override val messagesApi: MessagesApi,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      view: CheckYourAnswersView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getStructureFromIndex(request.userAnswers, businessId, index) match {
        case None => redirectJourneyRecovery(Some(s"No structure found with index: $index"))
        case Some(structure) =>
          val summaryList =
            SummaryListCYA.summaryListOpt(
              List(
                structure.qualifyingUse.map(StructuresBuildingsUseDateSummary.row(_, taxYear, businessId, index)),
                structure.newStructureBuildingQualifyingExpenditureAmount.map(
                  StructuresBuildingsQualifyingExpenditureSummary.row(_, taxYear, businessId, request.userType, index)),
                structure.newStructureBuildingLocation.map(StructureBuildingLocationSummary.row(_, taxYear, businessId, index)),
                structure.newStructureBuildingClaimingAmount.map(
                  StructuresBuildingsNewClaimAmountSummary.row(_, taxYear, businessId, request.userType, index))
              ))

          Ok(
            view(
              Page.cyaCheckYourAnswersHeading,
              taxYear,
              request.userType,
              summaryList,
              routes.StructuresBuildingsNewStructuresController.onPageLoad(taxYear, businessId)
            ))
      }
  }

}
