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

package controllers.journeys.capitalallowances.writingDownAllowance

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.handleSubmitAnswersResult
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.journeys.Journey.CapitalAllowancesWritingDownAllowance
import models.journeys.capitalallowances.writingDownAllowance.WritingDownAllowanceAnswers
import pages.Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.capitalallowances.writingDownAllowance._
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WritingDownAllowanceCYAController @Inject() (override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   identify: IdentifierAction,
                                                   getAnswers: DataRetrievalAction,
                                                   getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                                   requireAnswers: DataRequiredAction,
                                                   service: SelfEmploymentService,
                                                   view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {
  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getAnswers andThen getJourneyAnswers[WritingDownAllowanceAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, CapitalAllowancesWritingDownAllowance)) andThen requireAnswers) { implicit request =>
      val summaryList =
        SummaryListCYA(request.userAnswers, taxYear, businessId, request.userType).mkSummaryList(
          List(
            WdaSpecialRateSummary(taxYear, businessId),
            WdaSpecialRateClaimAmountSummary(taxYear, businessId),
            WdaMainRateSummary(taxYear, businessId),
            WdaMainRateClaimAmountSummary(taxYear, businessId),
            WdaSingleAssetSummary(taxYear, businessId),
            WdaSingleAssetClaimAmountsSummary(taxYear, businessId)
          )
        )

      Ok(
        view(
          Page.cyaCheckYourAnswersHeading,
          taxYear,
          request.userType,
          summaryList,
          routes.WritingDownAllowanceCYAController.onSubmit(taxYear, businessId)
        ))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireAnswers).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, businessId, CapitalAllowancesWritingDownAllowance)
      val result  = service.submitAnswers[WritingDownAllowanceAnswers](context, request.userAnswers)
      handleSubmitAnswersResult(context, result)
  }
}
