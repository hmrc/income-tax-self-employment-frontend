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
import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.common.JourneyStatus._
import models.common.{JourneyStatus, TaxYear}
import models.domain._
import models.errors.HttpError
import models.journeys.Journey.TradeDetails
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TradeJourneyStatusesViewModel
import views.html.journeys.TaskListView

import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    service: SelfEmploymentServiceBase,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  // TODO can we do one call to backend? Probably yes. Get List of completed
  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val result = (
      for {
        status          <- service.getJourneyStatus(TradeDetails, request.nino, taxYear, request.mtditid)
        completedTrades <- getViewModelList(taxYear, status)
        viewModelList: Seq[TradeJourneyStatusesViewModel] = completedTrades.map(TradesJourneyStatuses.toViewModel(_, taxYear, request.userAnswers))
      } yield Ok(view(taxYear, request.user, status, viewModelList))
    ).result

    result.merge
  }

  private def getViewModelList(taxYear: TaxYear, status: JourneyStatus)(implicit
      request: OptionalDataRequest[AnyContent]): EitherT[Future, HttpError, _ >: Nil.type <: List[TradesJourneyStatuses]] =
    status match {
      case Completed                                                  => service.getCompletedTradeDetails(request.nino, taxYear, request.mtditid)
      case CheckOurRecords | InProgress | CannotStartYet | NotStarted => EitherT.rightT[Future, HttpError](Nil)
    }
}
