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

package controllers.journeys.abroad

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import controllers.handleSubmitAnswersResult
import controllers.journeys.abroad
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.journeys.Journey
import models.journeys.Journey.Abroad
import models.journeys.abroad.SelfEmploymentAbroadAnswers
import pages.Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.abroad.SelfEmploymentAbroadSummary
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SelfEmploymentAbroadCYAController @Inject() (override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   identify: IdentifierAction,
                                                   getAnswers: DataRetrievalAction,
                                                   getJourneyAnswersIfAny: SubmittedDataRetrievalActionProvider,
                                                   requireData: DataRequiredAction,
                                                   service: SelfEmploymentService,
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
        Page.cyaHeadingKeyPrefix,
        taxYear,
        request.userType,
        summaryList,
        abroad.routes.SelfEmploymentAbroadCYAController.onSubmit(taxYear, businessId)
      )
    )
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getAnswers andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, Abroad)
      val result  = service.submitAnswers[SelfEmploymentAbroadAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
