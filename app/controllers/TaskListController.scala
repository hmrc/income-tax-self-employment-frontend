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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.requests.{OptionalDataRequest, TradesJourneyStatuses}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaskListView

import scala.concurrent.ExecutionContext

class TaskListController @Inject()(override val messagesApi: MessagesApi,
                                   selfEmploymentService: SelfEmploymentService,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: TaskListView)
                                  (implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request: OptionalDataRequest[AnyContent] =>

    selfEmploymentService.getCompletedTradeDetails(request.user.nino, taxYear, request.user.mtditid) map {

      case Right(list: Seq[TradesJourneyStatuses]) =>
        val viewModelList = list.map(TradesJourneyStatuses.toViewModel(_, taxYear))
        Ok(view(taxYear, request.user, viewModelList))

      case Left(_) =>
        Ok(view(taxYear, request.user, Seq.empty))
    }
  }
}
