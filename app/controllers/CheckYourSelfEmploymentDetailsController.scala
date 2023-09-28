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
import models.requests.BusinessData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SelfEmploymentDetailsViewModel
import views.html.CheckYourSelfEmploymentDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourSelfEmploymentDetailsController @Inject()(override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         selfEmploymentConnector: SelfEmploymentConnector,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: CheckYourSelfEmploymentDetailsView)
                                                        (implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String): Action[AnyContent] = (identify andThen getData) async { //TODO (Edem) does this need 'andThen requireData' ?
    implicit request =>

      val isAgent = request.user.isAgent
      selfEmploymentConnector.getBusiness(request.user.nino, businessId, request.user.mtditid) map {
        case Right(business: Seq[BusinessData]) =>
          val selfEmploymentDetails = SelfEmploymentDetailsViewModel.buildSummaryList(business.head, isAgent)
          Ok(view(selfEmploymentDetails, taxYear, if (isAgent) "agent" else "individual"))
        //TODO in View replace SaveAndContinue button's href to CompletedDetailsController when created
        //TODO in View replace RemoveSelfEmployment button's href to RemoveController when created
        case _ =>
          Redirect(routes.JourneyRecoveryController.onPageLoad().url)
      }
  }

}
