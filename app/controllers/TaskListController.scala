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
import models.requests.{BusinessData, OptionalDataRequest}
import models.viewModels.TaggedTradeDetailsViewModel
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaskListView

import scala.concurrent.ExecutionContext

class TaskListController @Inject()(override val messagesApi: MessagesApi,
                                   selfEmploymentService: SelfEmploymentService,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: TaskListView)
                                  (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request: OptionalDataRequest[AnyContent] =>

      // 1. Service to Connector to backend that returns a sequence of objects for each completed Business
      // 2. Backend needs to get all businesses, filter so only returning 'isCompleted = true' businessDatas
      // 3. Then return a sequence of these in on object that contains:
      //           (businessId: String, tradingName: Option[String], abroadStatus, incomeStatus, expensesStatus, nationalInsuranceStatus)

      selfEmploymentService.getCompletedTradeDetailsMock(request.user.nino, taxYear, request.user.mtditid) map {
        case Right(list: Seq[TaggedTradeDetailsViewModel]) => Ok(view(taxYear, request.user, list))
        case Left(_) => Ok(view(taxYear, request.user, Seq.empty))
      }
  }
}
