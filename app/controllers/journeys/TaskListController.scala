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
import controllers.actions.{DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import models.common.JourneyStatus._
import models.common.TaxYear
import models.domain._
import models.errors.ServiceError
import models.journeys.income.IncomeJourneyAnswers
import models.journeys.{Journey, TaskList}
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.TaskListView

import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    getJourneyAnswersIfAny: SubmittedDataRetrievalActionProvider,
                                    service: SelfEmploymentServiceBase,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  private def loadSubmittedAnswers[A: Format](taxYear: TaxYear, taskList: TaskList, request: OptionalDataRequest[AnyContent], journey: Journey) =
    taskList.businesses.foldLeft(Future.successful(request)) { (futureRequest, business) =>
      futureRequest.flatMap { updatedRequest =>
        getJourneyAnswersIfAny[A](req => req.mkJourneyNinoContext(taxYear, business.businessId, journey))
          .execute(updatedRequest)
      }
    }

  // TODO can we do one call to backend? Probably yes. Get List of statuses and list of answers needed for taskList. We may need a case class for TaskList model
  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getData) async { originalRequest =>
    val result = (
      for {
        taskList       <- service.getTaskList(originalRequest.nino, taxYear, originalRequest.mtditid)(hc(originalRequest))
        updatedRequest <- EitherT.right[ServiceError](loadSubmittedAnswers[IncomeJourneyAnswers](taxYear, taskList, originalRequest, Journey.Income))
        completedTrades = getViewModelList(taskList)
        message         = messagesApi.preferred(updatedRequest)
        viewModelList   = completedTrades.map(TradesJourneyStatuses.toViewModel(_, taxYear, updatedRequest.userAnswers)(message))
      } yield Ok(
        view(taxYear, updatedRequest.user, taskList.tradeDetails.map(_.journeyStatus).getOrElse(CheckOurRecords), viewModelList)(
          updatedRequest,
          message))
    ).result

    result.merge
  }

  private def getViewModelList(taskList: TaskList): List[TradesJourneyStatuses] =
    taskList.tradeDetails.map(_.journeyStatus).fold[List[TradesJourneyStatuses]](Nil) {
      case Completed                                                  => taskList.businesses
      case CheckOurRecords | InProgress | CannotStartYet | NotStarted => Nil
    }
}
