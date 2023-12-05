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

package controllers.journeys.expenses.repairsandmaintenance

import controllers.actions._
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountSummary, RepairsAndMaintenanceDisallowableAmountSummary}
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAView

import javax.inject.Inject

class RepairsAndMaintenanceCostsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    navigator: ExpensesNavigator,
    val controllerComponents: MessagesControllerComponents,
    view: RepairsAndMaintenanceCostsCYAView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator.nextNormalRoute(RepairsAndMaintenanceCostsCYAPage, request.userAnswers, taxYear, businessId)
    val summaryList = SummaryList(
      rows = List(
        RepairsAndMaintenanceAmountSummary.row(request, taxYear, businessId),
        RepairsAndMaintenanceDisallowableAmountSummary.row(request, taxYear, businessId)
      ).flatten
    )

    Ok(view(taxYear, request.userType, summaryList, nextRoute))
  }

}
