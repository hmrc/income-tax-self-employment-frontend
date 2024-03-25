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
import models.NormalMode
import viewmodels.checkAnswers.capitalallowances.specialTaxSites._
//import controllers.handleSubmitAnswersResult
import controllers.journeys
import models.common._
import models.journeys.Journey.CapitalAllowancesSpecialTaxSites
import pages.capitalallowances.tailoring.CapitalAllowancesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
//import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.capitalallowances.specialTaxSites.SpecialTaxSitesSummary
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
//import scala.concurrent.ExecutionContext

@Singleton
class SpecialTaxSitesCYAController @Inject() (override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getAnswers: DataRetrievalAction,
//                                              getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                              requireAnswers: DataRequiredAction,
//                                             service: SelfEmploymentService,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: CheckYourAnswersView)
//                                             (implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getAnswers andThen
//      getJourneyAnswers[SpecialTaxSitesAnswers](req =>
//      req.mkJourneyNinoContext(taxYear, businessId, CapitalAllowancesSpecialTaxSites)) andThen
      requireAnswers) { implicit request =>
      val summaryList =
        SummaryListCYA.summaryListOpt(
          List(
            SpecialTaxSitesSummary.row(request.userAnswers, taxYear, businessId, request.userType),
            NewSpecialTaxSitesSummary.row(request.userAnswers, taxYear, businessId),
            ExistingSiteClaimingAmountSummary.row(request.userAnswers, taxYear, businessId)
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

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers) { _ =>
    Redirect(
      journeys.routes.SectionCompletedStateController
        .onPageLoad(taxYear, businessId, CapitalAllowancesSpecialTaxSites.entryName, NormalMode))
//    implicit request =>
//      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, CapitalAllowancesSpecialTaxSites)
//      val result  = service.submitAnswers[SpecialTaxSitesAnswers](context, request.userAnswers)
//      handleSubmitAnswersResult(context, result)
  }

}
