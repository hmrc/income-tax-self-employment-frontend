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

package controllers.journeys.capitalallowances.specialTaxSites

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.redirectJourneyRecovery
import models.common._
import pages.capitalallowances.specialTaxSites.SpecialTaxSitesPage.getSiteFromIndex
import pages.capitalallowances.tailoring.CapitalAllowancesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.capitalallowances.specialTaxSites._
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}

@Singleton
class SiteSummaryController @Inject() (override val messagesApi: MessagesApi,
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
      getSiteFromIndex(request, businessId, index) match {
        case None => redirectJourneyRecovery()
        case Some(site) =>
          val summaryList =
            SummaryListCYA.summaryListOpt(
              List(
                site.contractForBuildingConstruction.map(ContractForBuildingConstructionSummary.row(_, taxYear, businessId, request.userType, index)),
                site.contractStartDate.map(ContractStartDateSummary.row(_, taxYear, businessId, index)),
                site.constructionStartDate.map(ConstructionStartDateSummary.row(_, taxYear, businessId, index)),
                site.qualifyingUseStartDate.map(QualifyingUseStartDateSummary.row(_, taxYear, businessId, index)),
                site.specialTaxSiteLocation.map(SpecialTaxSiteLocationSummary.row(_, taxYear, businessId, index)),
                site.newSiteClaimingAmount.map(NewSiteClaimingAmountSummary.row(_, taxYear, businessId, request.userType, index))
              ))

          Ok(
            view(
              CapitalAllowancesCYAPage,
              taxYear,
              request.userType,
              summaryList,
              routes.SpecialTaxSitesCYAController.onSubmit(taxYear, businessId)
            ))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { _ =>
    Redirect(routes.NewTaxSitesController.onPageLoad(taxYear, businessId))
  }

}
