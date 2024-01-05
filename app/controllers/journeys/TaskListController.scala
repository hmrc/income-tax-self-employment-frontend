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

import cats.implicits._
import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import models.common.JourneyStatus._
import models.common.TaxYear
import models.domain._
import models.journeys.TaskList
import models.requests.TradesJourneyStatuses
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.TaskListView

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton
class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    answerLoader: SubmittedDataRetrievalActionProvider,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getData) async { implicit originalRequest =>
    val result = (
      for {
        taskListWithRequest <- answerLoader.loadTaskList(taxYear, originalRequest)
        taskList           = taskListWithRequest.taskList
        updatedRequest     = taskListWithRequest.request
        completedTrades    = getViewModelList(taskList)
        message            = messagesApi.preferred(updatedRequest)
        viewModelList      = completedTrades.map(TradesJourneyStatuses.toViewModel(_, taxYear, updatedRequest.userAnswers)(message))
        tradeDetailsStatus = taskList.tradeDetails.map(_.journeyStatus).getOrElse(CheckOurRecords)
      } yield Ok(view(taxYear, updatedRequest.user, tradeDetailsStatus, viewModelList)(updatedRequest, message))
    ).result

    result.merge
  }

  private def getViewModelList(taskList: TaskList): List[TradesJourneyStatuses] =
    taskList.tradeDetails.map(_.journeyStatus).fold[List[TradesJourneyStatuses]](Nil) {
      case Completed                                                  => taskList.businesses
      case CheckOurRecords | InProgress | CannotStartYet | NotStarted => Nil
    }
}
