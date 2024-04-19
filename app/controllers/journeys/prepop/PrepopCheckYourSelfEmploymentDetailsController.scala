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

package controllers.journeys.prepop

import controllers.actions._
import controllers.handleResultT
import models.common.{BusinessId, TaxYear}
import models.domain.BusinessData
import pages.prepop.PrepopCheckYourSelfEmploymentDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.prepop.PrepopSelfEmploymentDetailsViewModel
import views.html.journeys.prepop.PrepopCheckYourSelfEmploymentDetailsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PrepopCheckYourSelfEmploymentDetailsController @Inject() (override val messagesApi: MessagesApi,
                                                                identify: IdentifierAction,
                                                                getData: DataRetrievalAction,
                                                                service: SelfEmploymentService,
                                                                val controllerComponents: MessagesControllerComponents,
                                                                view: PrepopCheckYourSelfEmploymentDetailsView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val result = service.getBusiness(request.nino, businessId, request.mtditid) map { business: BusinessData =>
      val selfEmploymentDetails = PrepopSelfEmploymentDetailsViewModel.buildSummaryList(business, request.userType)
      val nextRoute             = PrepopCheckYourSelfEmploymentDetailsPage.nextPage(taxYear, businessId).url

      Ok(view(selfEmploymentDetails, taxYear, request.userType, nextRoute))
    }
    handleResultT(result)
  }
}
