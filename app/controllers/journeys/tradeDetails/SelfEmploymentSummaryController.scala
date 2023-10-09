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

package controllers.journeys.tradeDetails

import connectors.SelfEmploymentConnector
import controllers.actions._
import controllers.journeys.tradeDetails.SelfEmploymentSummaryController.generateRowList
import handlers.ErrorHandler
import models.requests.OptionalDataRequest
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.SelfEmploymentSummaryPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.summary.SelfEmploymentSummaryViewModel.row
import views.html.journeys.tradeDetails.SelfEmploymentSummaryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelfEmploymentSummaryController @Inject() (override val messagesApi: MessagesApi,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 errorHandler: ErrorHandler,
                                                 selfEmploymentConnector: SelfEmploymentConnector,
                                                 navigator: Navigator,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: SelfEmploymentSummaryView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentConnector.getBusinesses(request.user.nino, request.user.mtditid).map {
      case Left(_) => errorHandler.internalServerError()
      case Right(model) =>
        val viewModel = generateRowList(taxYear, model.map(bd => (bd.tradingName.getOrElse(""), bd.businessId)))
        val nextRoute = navigate(taxYear, navigator)
        Ok(view(taxYear, viewModel, nextRoute))
    }
  }

  private def navigate(taxYear: Int, navigator: Navigator)(implicit request: OptionalDataRequest[AnyContent]) = {
    navigator.nextPage(SelfEmploymentSummaryPage, NormalMode, taxYear, request.userAnswers.getOrElse(UserAnswers(request.userId))).url
  }

}

object SelfEmploymentSummaryController {

  def generateRowList(taxYear: Int, model: Seq[(String, String)])(implicit messages: Messages) = {
    SummaryList(rows = model.map { case (tradingName, businessId) =>
      row(tradingName, routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url)
    })
  }

}
