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

import com.google.inject.Inject
import connectors.SelfEmploymentConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.standard.routes.JourneyRecoveryController
import models.journeys.Journey.TradeDetails
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TradeJourneyStatusesViewModel
import views.html.journeys.TaskListView

import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    selfEmploymentService: SelfEmploymentService,
                                    selfEmploymentConnector: SelfEmploymentConnector,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    for {
      statusMsg <- getStatusMsg(taxYear, selfEmploymentConnector)
      viewModelList <-
        if (statusMsg.exists(_.equals("completed"))) getViewModelList(taxYear)
        else Future(Some(Seq.empty))
    } yield
      if (statusMsg.isEmpty || viewModelList.isEmpty) Redirect(JourneyRecoveryController.onPageLoad())
      else Ok(view(taxYear, request.user, statusMsg.get, viewModelList.get))
  }

  private def getStatusMsg(taxYear: Int, selfEmploymentConnector: SelfEmploymentConnector)(implicit
      request: OptionalDataRequest[AnyContent],
      ec: ExecutionContext): Future[Option[String]] = {

    val journey = TradeDetails.toString
    val tradeId = journey + "-" + request.user.nino

    selfEmploymentConnector.getJourneyState(tradeId, journey, taxYear, request.user.mtditid) map {
      case Left(_) => None
      case Right(status) =>
        Some(
          if (status.isEmpty) "checkOurRecords"
          else if (status.get) "completed"
          else "inProgress"
        )
    }
  }

  private def getViewModelList(taxYear: Int)(implicit request: OptionalDataRequest[AnyContent]): Future[Option[Seq[TradeJourneyStatusesViewModel]]] =
    selfEmploymentService.getCompletedTradeDetails(request.user.nino, taxYear, request.user.mtditid) map {
      case Left(_) => None
      case Right(list) =>
        Some(
          if (list.isEmpty) Seq.empty else list.map(TradesJourneyStatuses.toViewModel(_, taxYear))
        )
    }
}
