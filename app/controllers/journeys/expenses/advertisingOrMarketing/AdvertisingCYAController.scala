/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.advertisingOrMarketing

import controllers.actions._
import controllers.handleSubmitAnswersResult
import models.common._
import models.common.Journey.ExpensesAdvertisingOrMarketing
import models.journeys.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import pages.expenses.advertisingOrMarketing.AdvertisingOrMarketingCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.advertisingOrMarketing.{AdvertisingAmountSummary, AdvertisingDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AdvertisingCYAController @Inject() (override val messagesApi: MessagesApi,
                                          identify: IdentifierAction,
                                          getUserAnswers: DataRetrievalAction,
                                          getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                          requireData: DataRequiredAction,
                                          service: SelfEmploymentService,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getUserAnswers andThen getJourneyAnswers[AdvertisingOrMarketingJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesAdvertisingOrMarketing)) andThen requireData) { implicit request =>
      val user = request.userType

      val summaryList = SummaryListCYA.summaryListOpt(
        List(
          AdvertisingAmountSummary.row(request.userAnswers, taxYear, businessId, user),
          AdvertisingDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, user)
        )
      )

      Ok(view(AdvertisingOrMarketingCYAPage.toString, taxYear, user, summaryList, routes.AdvertisingCYAController.onSubmit(taxYear, businessId)))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getUserAnswers andThen requireData) async {
    implicit request =>
      val context =
        JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesAdvertisingOrMarketing)
      val result = service.submitAnswers[AdvertisingOrMarketingJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
