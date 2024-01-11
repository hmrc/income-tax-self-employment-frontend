/*
 * Copyright 2024 HM Revenue & Customs
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
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.domain.BusinessData
import models.requests.OptionalDataRequest
import navigation.TradeDetailsNavigator
import pages.tradeDetails.CheckYourSelfEmploymentDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.tradeDetails.SelfEmploymentDetailsViewModel
import views.html.journeys.tradeDetails.CheckYourSelfEmploymentDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourSelfEmploymentDetailsController @Inject() (override val messagesApi: MessagesApi,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          selfEmploymentConnector: SelfEmploymentConnector,
                                                          navigator: TradeDetailsNavigator,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: CheckYourSelfEmploymentDetailsView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentConnector.getBusiness(request.user.nino, businessId, request.user.mtditid) map {
      case Right(business: Seq[BusinessData]) =>
        val selfEmploymentDetails = SelfEmploymentDetailsViewModel.buildSummaryList(business.head, request.userType)
        val nextRoute             = navigate(taxYear, businessId, navigator)
        Ok(view(selfEmploymentDetails, taxYear, request.userType, nextRoute))
      // TODO in View replace RemoveSelfEmployment button's href to RemoveController when created
      case _ =>
        Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad().url)
    }
  }

  private def navigate(taxYear: TaxYear, businessId: BusinessId, navigator: TradeDetailsNavigator)(implicit
      request: OptionalDataRequest[AnyContent]): String =
    navigator
      .nextPage(CheckYourSelfEmploymentDetailsPage, NormalMode, request.userAnswers.getOrElse(UserAnswers(request.userId)), taxYear, businessId)
      .url

}
