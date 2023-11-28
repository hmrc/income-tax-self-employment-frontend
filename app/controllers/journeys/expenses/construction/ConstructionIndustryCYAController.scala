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

package controllers.journeys.expenses.construction

import controllers.actions._
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.construction.ConstructionIndustryCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.construction.ConstructionIndustryAmountSummary
import views.html.journeys.expenses.construction.ConstructionIndustryCYAView

import javax.inject.Inject

class ConstructionIndustryCYAController @Inject() (override val messagesApi: MessagesApi,
                                            navigator: ExpensesNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ConstructionIndustryCYAView)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator
      .nextPage(ConstructionIndustryCYAPage, NormalMode, request.userAnswers, taxYear, businessId.value)
      .url

    val summaryList = SummaryList(
      rows = Seq(
        ConstructionIndustryAmountSummary.row(request.userAnswers, taxYear, businessId, request.user.userType)
      ).flatten,
      classes = "govuk-!-margin-bottom-7"
    )

    Ok(view(taxYear, request.user.userType, summaryList, nextRoute))
  }

}
