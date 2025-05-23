/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.journeys

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.handleResultT
import models.common.{TaxYear, TradingName}
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.TaskListWithRequest
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.journeys.taskList.{NationalInsuranceContributionsViewModel, TradeJourneyStatusesViewModel}
import views.html.journeys.TaskListView

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton
class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    service: SelfEmploymentService,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    answerLoader: SubmittedDataRetrievalActionProvider,
                                    appConfig: FrontendAppConfig,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getData) async { implicit originalRequest =>
    val result = for {
      taskListWithRequest <- answerLoader.loadTaskList(taxYear, originalRequest)
      updatedRequest  = taskListWithRequest.request
      messages        = messagesApi.preferred(updatedRequest)
      completedTrades = taskListWithRequest.taskList.businesses
      businessSummaryList      <- saveAndGetBusinessSummaries(completedTrades, updatedRequest, taxYear, messages)
      nationalInsuranceSummary <- getNationalInsuranceSummary(taskListWithRequest, completedTrades, taxYear, messages)
    } yield Ok(view(taxYear, updatedRequest.user, businessSummaryList, nationalInsuranceSummary)(updatedRequest, messages))
    handleResultT(result)
  }

  private def saveAndGetBusinessSummaries(completedTrades: List[TradesJourneyStatuses],
                                          request: OptionalDataRequest[_],
                                          taxYear: TaxYear,
                                          messages: Messages): ApiResultT[Seq[TradeJourneyStatusesViewModel]] = {
    val matchIdsWithAccountingType = completedTrades.map(t => (t.tradingName.getOrElse(TradingName.empty), t.accountingType, t.businessId))

    EitherT.right[ServiceError](service.setAccountingTypeForIds(request.answers, matchIdsWithAccountingType)).map { updatedUserAnswers =>
      completedTrades.map(TradesJourneyStatuses.toViewModel(_, taxYear, updatedUserAnswers.some, appConfig)(messages))
    }
  }

  private def getNationalInsuranceSummary(taskListWithRequest: TaskListWithRequest,
                                          completedTrades: List[TradesJourneyStatuses],
                                          taxYear: TaxYear,
                                          messages: Messages)(implicit hc: HeaderCarrier): ApiResultT[SummaryList] = {
    val nino    = taskListWithRequest.request.nino
    val mtditid = taskListWithRequest.request.mtditid

    for {
      userDateOfBirth            <- service.getUserDateOfBirth(nino, mtditid)
      allBusinessesProfitAndLoss <- service.getAllBusinessesTaxableProfitAndLoss(taxYear, nino, mtditid)
      nationalInsuranceStatuses = taskListWithRequest.taskList.nationalInsuranceContributions
    } yield NationalInsuranceContributionsViewModel.buildSummaryList(
      nationalInsuranceStatuses,
      completedTrades,
      userDateOfBirth,
      allBusinessesProfitAndLoss,
      taxYear)(messages)
  }
}
