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

package controllers.journeys.expenses.officeSupplies

import controllers.actions._
import controllers.handleSubmitAnswersResult
import models.common.ModelUtils.userType
import models.common._
import models.journeys.Journey.ExpensesOfficeSupplies
import models.journeys.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.expenses.officeSupplies.{OfficeSuppliesAmountSummary, OfficeSuppliesDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class OfficeSuppliesCYAController @Inject() (override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             service: SelfEmploymentService,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: OfficeSuppliesCYAView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val authUser = userType(request.user.isAgent)

    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        OfficeSuppliesAmountSummary.row(request.userAnswers, taxYear, businessId, authUser),
        OfficeSuppliesDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, authUser)
      )
    )

    Ok(view(authUser, summaryList, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val context = JourneyAnswersWithNino(taxYear, Nino(request.user.nino), businessId, Mtditid(request.user.mtditid), ExpensesOfficeSupplies)
      val result  = service.submitAnswers[OfficeSuppliesJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

}
