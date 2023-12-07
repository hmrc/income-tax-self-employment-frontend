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

package controllers.journeys.expenses.staffCosts

import controllers.actions._
import controllers.handleSubmitAnswersResult
import models.common._
import models.journeys.Journey.ExpensesStaffCosts
import models.journeys.expenses.staffCosts.StaffCostsJourneyAnswers
import pages.expenses.staffCosts.StaffCostsCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.staffCosts._
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StaffCostsCYAController @Inject() (override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         service: SelfEmploymentService,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val summaryList = SummaryListCYA.summaryListOpt(
      rows = List(
        StaffCostsAmountSummary.row(request, taxYear, businessId),
        StaffCostsDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
      )
    )

    Ok(view(StaffCostsCYAPage.toString, taxYear, request.userType, summaryList, routes.StaffCostsCYAController.onSubmit(taxYear, businessId)))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, Nino(request.user.nino), businessId, Mtditid(request.user.mtditid), ExpensesStaffCosts)
      val result  = service.submitAnswers[StaffCostsJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
