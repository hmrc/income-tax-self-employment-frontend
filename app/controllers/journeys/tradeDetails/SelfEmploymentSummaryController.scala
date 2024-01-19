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

import controllers.actions._
import controllers.handleResultT
import controllers.journeys.tradeDetails.SelfEmploymentSummaryController.generateRowList
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.domain.BusinessData
import models.requests.OptionalDataRequest
import navigation.TradeDetailsNavigator
import pages.tradeDetails.SelfEmploymentSummaryPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.tradeDetails.SelfEmploymentSummaryViewModel.row
import views.html.journeys.tradeDetails.SelfEmploymentSummaryView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SelfEmploymentSummaryController @Inject() (override val messagesApi: MessagesApi,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 service: SelfEmploymentService,
                                                 navigator: TradeDetailsNavigator,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: SelfEmploymentSummaryView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val result = service.getBusinesses(request.nino, request.mtditid).map { businesses: Seq[BusinessData] =>
      val viewModel = generateRowList(taxYear, businesses.map(bd => (bd.tradingName.getOrElse(""), BusinessId(bd.businessId))))
      val nextRoute = navigate(taxYear, navigator)
      Ok(view(viewModel, nextRoute))
    }
    handleResultT(result)
  }

  private def navigate(taxYear: TaxYear, navigator: TradeDetailsNavigator)(implicit request: OptionalDataRequest[AnyContent]): String = {
    val businessId = BusinessId.tradeDetailsId
    navigator
      .nextPage(SelfEmploymentSummaryPage, NormalMode, request.userAnswers.getOrElse(UserAnswers(request.userId)), taxYear, businessId)
      .url
  }

}

object SelfEmploymentSummaryController {

  def generateRowList(taxYear: TaxYear, model: Seq[(String, BusinessId)])(implicit messages: Messages): SummaryList =
    SummaryList(rows = model.map { case (tradingName, businessId) =>
      row(tradingName, routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url)
    })

}
