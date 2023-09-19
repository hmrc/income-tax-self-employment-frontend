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
import connectors.SelfEmploymentConnector
import controllers.actions._
import handlers.ErrorHandler
import models.requests.OptionalDataRequest

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SelfEmploymentSummaryView

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class SelfEmploymentSummaryController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       errorHandler: ErrorHandler,
                                       selfEmploymentConnector: SelfEmploymentConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: SelfEmploymentSummaryView,
                                       ec: ExecutionContext
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) async {
    implicit request: OptionalDataRequest[AnyContent] =>
      selfEmploymentConnector.getBusinesses(request.nino).map {
        case Left(_) =>  errorHandler.internalServerError()
        case Right(value) =>
          val business = value.tradingName.getOrElse("")
          Ok(view(business))
      }
  }
}