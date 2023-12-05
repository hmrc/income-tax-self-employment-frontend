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
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.staffCosts.StaffCostsCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.staffCosts._
import views.html.journeys.expenses.staffCosts.StaffCostsCYAView

import javax.inject.Inject

class StaffCostsCYAController @Inject() (override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         navigator: ExpensesNavigator,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: StaffCostsCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator.nextNormalRoute(StaffCostsCYAPage, request.userAnswers, taxYear, businessId)
    val summaryList = SummaryList(
      rows = List(
        StaffCostsAmountSummary.row(request, taxYear, businessId),
        StaffCostsDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, request.userType)
      ).flatten
    )

    Ok(view(taxYear, request.userType, summaryList, nextRoute))
  }

}
