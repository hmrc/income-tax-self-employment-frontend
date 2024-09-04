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

package controllers.journeys.adjustments.profitOrLoss

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.handleSubmitAnswersResult
import models.common.{AccountingType, BusinessId, JourneyContextWithNino, TaxYear}
import pages.Page
import models.CheckMode
import models.common.Journey.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLossJourneyAnswers
import pages.adjustments.profitOrLoss.{GoodsAndServicesAmountPage, GoodsAndServicesForYourOwnUsePage, PreviousUnusedLossesPage, UnusedLossAmountPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.adjustments.WhichYearIsLossReportedSummary
import viewmodels.checkAnswers.{BigDecimalSummary, BooleanSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProfitOrLossCYAController @Inject() (override val messagesApi: MessagesApi,
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

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getAnswers andThen getJourneyAnswers[ProfitOrLossJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ProfitOrLoss)) andThen requireData) { implicit request =>
      val accountingType                      = request.userAnswers.getAccountingType(businessId)
      val accountingTypePrefix                = if (accountingType == AccountingType.Cash) ".cash" else ""
      val goodsAndServicesAmountKeyMessage    = Messages(s"goodsAndServicesAmount.subHeading.cya$accountingTypePrefix.${request.userType}")
      val goodsAndServicesAmountChangeMessage = Messages(s"goodsAndServicesAmount.change$accountingTypePrefix.hidden")

      val previousUnusedLossesSummaryKeyMessage =
        Messages(s"previousUnusedLosses.subHeading.cya.${request.userType}", request.userAnswers.getTraderName(businessId).value)
      val previousUnusedLossesSummaryChangeMessage =
        Messages(s"previousUnusedLosses.change.hidden", request.userAnswers.getTraderName(businessId).value)

      val summaryList = SummaryListCYA.summaryListOpt(
        List(
          new BooleanSummary(
            GoodsAndServicesForYourOwnUsePage,
            routes.GoodsAndServicesForYourOwnUseController.onPageLoad(taxYear, businessId, CheckMode))
            .row(request.userAnswers, taxYear, businessId, request.userType),
          new BigDecimalSummary(GoodsAndServicesAmountPage, routes.GoodsAndServicesAmountController.onPageLoad(taxYear, businessId, CheckMode))
            .row(
              request.userAnswers,
              taxYear,
              businessId,
              request.userType,
              overrideKeyMessage = Some(goodsAndServicesAmountKeyMessage),
              overrideChangeMessage = Some(goodsAndServicesAmountChangeMessage)
            ),
          new BooleanSummary(PreviousUnusedLossesPage, routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, CheckMode))
            .row(
              request.userAnswers,
              taxYear,
              businessId,
              request.userType,
              overrideKeyMessage = Some(previousUnusedLossesSummaryKeyMessage),
              overrideChangeMessage = Some(previousUnusedLossesSummaryChangeMessage)
            ),
          new BigDecimalSummary(UnusedLossAmountPage, routes.UnusedLossAmountController.onPageLoad(taxYear, businessId, CheckMode))
            .row(request.userAnswers, taxYear, businessId, request.userType),
          WhichYearIsLossReportedSummary.row(request.userAnswers, request.userType, taxYear, businessId)
        ))

      Ok(
        view(
          Page.cyaCheckYourAnswersHeading,
          taxYear,
          request.userType,
          summaryList,
          routes.ProfitOrLossCYAController.onSubmit(taxYear, businessId)
        )
      )
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ProfitOrLoss)
      val result  = service.submitAnswers[ProfitOrLossJourneyAnswers](context, request.userAnswers)
      handleSubmitAnswersResult(context, result)
  }
}
