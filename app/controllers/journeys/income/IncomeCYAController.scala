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

package controllers.journeys.income

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.income.IncomeCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.income._
import views.html.journeys.income.IncomeCYAView

import javax.inject.Inject

class IncomeCYAController @Inject() (override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     navigator: Navigator,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: IncomeCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator
      .nextPage(IncomeCYAPage, NormalMode, request.userAnswers, taxYear)
      .url

    val summaryList = SummaryList(
      rows = Seq(
        AnyOtherIncomeSummary.row(request.userAnswers, taxYear, authUserType(request)),
        IncomeNotCountedAsTurnoverSummary.row(request.userAnswers, taxYear, authUserType(request)),
        TradingAllowanceSummary.row(request.userAnswers, taxYear, authUserType(request)),
        TurnoverIncomeAmountSummary.row(request.userAnswers, taxYear, authUserType(request)),
        HowMuchTradingAllowanceSummary.row(request.userAnswers, taxYear, authUserType(request)),
        NonTurnoverIncomeAmountSummary.row(request.userAnswers, taxYear, authUserType(request)),
        NotTaxableAmountSummary.row(request.userAnswers, taxYear, authUserType(request)),
        OtherIncomeAmountSummary.row(request.userAnswers, taxYear, authUserType(request)),
        TradingAllowanceAmountSummary.row(request.userAnswers, taxYear, authUserType(request)),
        TurnoverNotTaxableSummary.row(request.userAnswers, taxYear, authUserType(request))
      ).flatten,
      classes = "govuk-!-margin-bottom-7"
    )

    Ok(view(taxYear, summaryList, nextRoute, authUserType(request)))
  }

  private def authUserType(request: DataRequest[AnyContent]): String =
    if (request.user.isAgent) "agent" else "individual"

}
