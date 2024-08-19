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

package controllers.journeys.nics

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.{handleResultT, handleSubmitAnswersResult}
import models.CheckMode
import models.common.BusinessId.nationalInsuranceContributions
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.domain.BusinessData
import models.journeys.Journey.NationalInsuranceContributions
import models.journeys.nics.NICsJourneyAnswers
import pages.Page
import pages.nics._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.BooleanSummary
import viewmodels.checkAnswers.nics.{Class4DivingExemptSummary, Class4ExemptionReasonSummary, Class4NonDivingExemptSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NICsCYAController @Inject() (override val messagesApi: MessagesApi,
                                   val controllerComponents: MessagesControllerComponents,
                                   identify: IdentifierAction,
                                   getAnswers: DataRetrievalAction,
                                   getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                   requireData: DataRequiredAction,
                                   service: SelfEmploymentService,
                                   view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getAnswers andThen getJourneyAnswers[NICsJourneyAnswers](req =>
    req.mkJourneyNinoContext(taxYear, nationalInsuranceContributions, NationalInsuranceContributions)) andThen requireData) async {
    implicit request =>
      val result = service.getBusinesses(request.nino, request.mtditid).map { businesses: Seq[BusinessData] =>
        val summaryList = SummaryListCYA.summaryListOpt(
          List(
            new BooleanSummary(Class2NICsPage, routes.Class2NICsController.onPageLoad(taxYear, CheckMode))
              .row(request.userAnswers, taxYear, nationalInsuranceContributions, request.userType, rightTextAlign = false),
            new BooleanSummary(Class4NICsPage, routes.Class4NICsController.onPageLoad(taxYear, CheckMode))
              .row(request.userAnswers, taxYear, nationalInsuranceContributions, request.userType, rightTextAlign = false),
            Class4ExemptionReasonSummary.row(request.userAnswers, request.userType, taxYear),
            Class4DivingExemptSummary.row(request.userAnswers, businesses, request.userType, taxYear),
            Class4NonDivingExemptSummary.row(request.userAnswers, businesses, request.userType, taxYear)
          ))

        Ok(view(Page.cyaCheckYourAnswersHeading, taxYear, request.userType, summaryList, routes.NICsCYAController.onSubmit(taxYear)))
      }

      handleResultT(result)
  }

  def onSubmit(taxYear: TaxYear): Action[AnyContent] = (identify andThen getAnswers andThen requireData) async { implicit request =>
    val maybeSingleBusinessId: Option[BusinessId] = request.userAnswers.getBusinesses.headOption.map(_.businessId)
    val idForContext: BusinessId                  = maybeSingleBusinessId.getOrElse(BusinessId.nationalInsuranceContributions)
    val context =
      JourneyContextWithNino(taxYear, request.nino, idForContext, request.mtditid, NationalInsuranceContributions)
    val result = service.submitAnswers[NICsJourneyAnswers](context, request.userAnswers)
    handleSubmitAnswersResult(context, result)
  }
}
