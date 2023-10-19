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
import models.mdtp.BusinessData
import models.requests.OptionalDataRequest
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.CheckYourSelfEmploymentDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.tradeDetails.SelfEmploymentDetailsViewModel
import views.html.journeys.tradeDetails.CheckYourSelfEmploymentDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourSelfEmploymentDetailsController @Inject()(override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         selfEmploymentConnector: SelfEmploymentConnector,
                                                         navigator: Navigator,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: CheckYourSelfEmploymentDetailsView)
                                                        (implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String): Action[AnyContent] = (identify andThen getData) async { //TODO Business does this need 'andThen requireData' ?
    implicit request =>

      val isAgent = request.user.isAgent
      selfEmploymentConnector.getBusiness(request.user.nino, businessId, request.user.mtditid) map {
        case Right(business: Seq[BusinessData]) =>
          val selfEmploymentDetails = SelfEmploymentDetailsViewModel.buildSummaryList(business.head, isAgent)
          val nextRoute = navigate(taxYear, businessId, navigator)
          Ok(view(selfEmploymentDetails, taxYear, if (isAgent) "agent" else "individual", nextRoute))
        //TODO in View replace RemoveSelfEmployment button's href to RemoveController when created
        case _ =>
          Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad().url)
      }
  }
  
  private def navigate(taxYear: Int, businessId: String, navigator: Navigator)(implicit request: OptionalDataRequest[AnyContent]): String = {
    navigator.nextPage(CheckYourSelfEmploymentDetailsPage, NormalMode, request.userAnswers.getOrElse(UserAnswers(request.userId)), taxYear, Some(businessId)).url
  }

}
