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
import models.TradeDetails
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.TaskListView

import scala.concurrent.ExecutionContext

class TaskListController @Inject() (override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    selfEmploymentConnector: SelfEmploymentConnector,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: TaskListView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    getStatusMsg(taxYear, selfEmploymentConnector) map {
      case Right(Some(true))  => Ok(view(taxYear, "status.complete"))
      case Right(Some(false)) => Ok(view(taxYear, "status.processing"))
      case Right(None)        => Ok(view(taxYear, "status.checkOurRecords"))
      case Left(_)            => Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def getStatusMsg(taxYear: Int, selfEmploymentConnector: SelfEmploymentConnector)(implicit
      request: OptionalDataRequest[AnyContent],
      ec: ExecutionContext) = {

    val journey = TradeDetails.toString
    val tradeId = journey + "-" + request.user.nino

    selfEmploymentConnector.getJourneyState(tradeId, journey, taxYear, request.user.mtditid)
  }

}
