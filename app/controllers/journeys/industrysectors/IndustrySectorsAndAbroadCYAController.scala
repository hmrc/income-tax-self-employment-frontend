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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.journeys
import models.NormalMode
import models.common.Journey.IndustrySectors
import models.common.{BusinessId, Journey, TaxYear}
import models.journeys.abroad.SelfEmploymentAbroadAnswers
import pages.Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.industrysectors.SelfEmploymentAbroadSummary
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndustrySectorsAndAbroadCYAController @Inject() (override val messagesApi: MessagesApi,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       identify: IdentifierAction,
                                                       getAnswers: DataRetrievalAction,
                                                       getJourneyAnswersIfAny: SubmittedDataRetrievalActionProvider,
                                                       requireData: DataRequiredAction,
                                                       view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen
    getJourneyAnswersIfAny[SelfEmploymentAbroadAnswers](request =>
      request.mkJourneyNinoContext(taxYear, businessId, Journey.Abroad)) andThen requireData) { implicit request =>
    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        SelfEmploymentAbroadSummary.row(taxYear, request.user.userType, businessId, request.userAnswers)
      )
    )

    Ok(
      view(
        Page.cyaCheckYourDetailsHeading,
        taxYear,
        request.userType,
        summaryList,
        routes.IndustrySectorsAndAbroadCYAController.onSubmit(taxYear, businessId)
      )
    )
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireData).async {
    Future.successful(Redirect(journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, IndustrySectors, NormalMode)))
  }

}
