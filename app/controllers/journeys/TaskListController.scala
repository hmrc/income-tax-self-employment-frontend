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
import connectors.httpParser.JourneyStateParser.JourneyStateResponse
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.standard.routes.JourneyRecoveryController
import models.TradeDetails
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.TaskListView

import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject()(override val messagesApi: MessagesApi,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   selfEmploymentService: SelfEmploymentService,
                                   selfEmploymentConnector: SelfEmploymentConnector,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: TaskListView)
                                  (implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request =>

    val msgStatus = getStatusMsg(taxYear, selfEmploymentConnector) map {
      case Left(_) => "error" //TODO change this from error
      case Right(status) => if (status.isEmpty) "checkOurRecords" else if (status.get) "completed" else "inProgress"
    }
    val viewModelList = selfEmploymentService.getCompletedTradeDetails(request.user.nino, taxYear, request.user.mtditid) map {
      case Left(_) => Seq.empty //TODO change this to error
      case Right(list: Seq[TradesJourneyStatuses]) =>
        if (list.isEmpty) Seq.empty else list.map(TradesJourneyStatuses.toViewModel(_, taxYear))
    }
    for {
      statusMsg <- msgStatus
      viewModelList <- viewModelList
    } yield {
      if (statusMsg.equals("error")) Redirect(JourneyRecoveryController.onPageLoad())
      else Ok(view(taxYear, request.user, statusMsg, viewModelList))
    }
  }


  private def getStatusMsg(taxYear: Int, selfEmploymentConnector: SelfEmploymentConnector)
                          (implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext): Future[JourneyStateResponse] = {

    val journey = TradeDetails.toString
    val tradeId = journey + "-" + request.user.nino

    selfEmploymentConnector.getJourneyState(tradeId, journey, taxYear, request.user.mtditid)
  }
}
