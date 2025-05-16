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

package controllers.journeys.industrysectors

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys
import controllers.journeys.industrysectors
import models.NormalMode
import models.common.Journey.IndustrySectors
import models.common.{BusinessId, TaxYear}
import models.journeys.industrySectors.IndustrySectorsDb
import pages.Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.industrysectors.{FarmerOrMarketGardenerSummary, LiteraryOrCreativeWorksSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndustrySectorsAndAbroadCYAController @Inject() (override val messagesApi: MessagesApi,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       identify: IdentifierAction,
                                                       getAnswers: DataRetrievalAction,
                                                       answersService: AnswersService,
                                                       requireData: DataRequiredAction,
                                                       view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen
    requireData).async { implicit request =>
    val ctx = request.mkJourneyNinoContext(taxYear, businessId, IndustrySectors)
    answersService.getAnswers[IndustrySectorsDb](ctx).map {
      case Some(model) =>
        val summaryList = SummaryListCYA.summaryListOpt(
          List(
            FarmerOrMarketGardenerSummary.row(taxYear, businessId, request.userType, model),
            LiteraryOrCreativeWorksSummary.row(taxYear, businessId, request.userType, model)
          )
        )

        Ok(
          view(
            Page.cyaCheckYourDetailsHeading,
            taxYear,
            request.userType,
            summaryList,
            industrysectors.routes.IndustrySectorsAndAbroadCYAController.onSubmit(taxYear, businessId)
          )
        )
      case None =>
        Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireData).async {
    Future.successful(Redirect(journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, IndustrySectors, NormalMode)))
  }

}
