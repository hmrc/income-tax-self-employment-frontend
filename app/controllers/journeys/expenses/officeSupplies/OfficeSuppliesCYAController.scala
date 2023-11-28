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
import models.NormalMode
import models.common.ModelUtils.userType
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.officeSupplies.OfficeSuppliesCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.officeSupplies.{OfficeSuppliesAmountSummary, OfficeSuppliesDisallowableAmountSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

import javax.inject.Inject

class OfficeSuppliesCYAController @Inject() (override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             navigator: ExpensesNavigator,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: OfficeSuppliesCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator
      .nextPage(OfficeSuppliesCYAPage, NormalMode, request.userAnswers, taxYear, BusinessId(businessId))
      .url

    val authUser = userType(request.user.isAgent)

    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        OfficeSuppliesAmountSummary.row(request.userAnswers, taxYear, businessId, authUser),
        OfficeSuppliesDisallowableAmountSummary.row(request.userAnswers, taxYear, businessId, authUser)
      )
    )

    Ok(view(authUser, summaryList, taxYear, nextRoute))
  }

}
