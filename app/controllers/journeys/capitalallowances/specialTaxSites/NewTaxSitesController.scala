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
import controllers.{journeys, redirectJourneyRecovery}
import forms.standard.BooleanFormProvider
import models.NormalMode
import models.common._
import models.common.Journey.CapitalAllowancesSpecialTaxSites
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.returnTotalIfMultipleSites
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesAnswers.removeIncompleteSites
import pages.capitalallowances.specialTaxSites._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepositoryBase
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.journeys.capitalallowances.specialTaxSites.NewTaxSitesViewModel.getNewSitesSummaryRows
import views.html.journeys.capitalallowances.specialTaxSites.NewTaxSitesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NewTaxSitesController @Inject() (override val messagesApi: MessagesApi,
                                       val controllerComponents: MessagesControllerComponents,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       service: SelfEmploymentService,
                                       sessionRepository: SessionRepositoryBase,
                                       formProvider: BooleanFormProvider,
                                       view: NewTaxSitesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = NewTaxSitesPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request.getValue(NewSpecialTaxSitesList, businessId) match {
        case None => Future(redirectJourneyRecovery())
        case Some(sites) =>
          val cleanSiteList = removeIncompleteSites(sites)
          service.persistAnswer(businessId, request.userAnswers, cleanSiteList, NewSpecialTaxSitesList).map { _ =>
            val summaryList = getNewSitesSummaryRows(cleanSiteList, taxYear, businessId)
            val totalAmount = returnTotalIfMultipleSites(cleanSiteList)
            Ok(view(formProvider(page, request.userType), request.userType, taxYear, businessId, summaryList, totalAmount))
          }
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.getValue(NewSpecialTaxSitesList, businessId) match {
      case None => redirectJourneyRecovery()
      case Some(sites) =>
        val summaryList = getNewSitesSummaryRows(sites, taxYear, businessId)
        formProvider(page, request.userType)
          .bindFromRequest()
          .fold(
            formErrors => BadRequest(view(formErrors, request.userType, taxYear, businessId, summaryList, returnTotalIfMultipleSites(sites))),
            answer =>
              Redirect(
                if (answer) routes.ContractForBuildingConstructionController.onPageLoad(taxYear, businessId, sites.length, NormalMode)
                else routes.DoYouHaveAContinuingClaimController.onPageLoad(taxYear, businessId, NormalMode)
              )
          )
    }
  }

  def returnToOverview(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      for {
        updatedAnswers <- Future.fromTry(
          SelfEmploymentService.clearDataFromUserAnswers(request.userAnswers, CapitalAllowancesSpecialTaxSites.answerPages, Some(businessId)))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(journeys.routes.TaskListController.onPageLoad(taxYear).url)
  }

}
