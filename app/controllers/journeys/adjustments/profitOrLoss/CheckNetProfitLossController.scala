/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.adjustments.profitOrLoss

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.NormalMode
import models.common._
import models.journeys.adjustments.ProfitOrLoss.returnProfitOrLoss
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{buildTable1, buildTable2, buildTable3}
import views.html.journeys.adjustments.profitOrLoss.CheckNetProfitLossView

import javax.inject.{Inject, Singleton}

@Singleton
class CheckNetProfitLossController @Inject() (override val messagesApi: MessagesApi,
                                              val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              view: CheckNetProfitLossView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val netAmount          = BigDecimal(-200)
    val profitOrLoss       = returnProfitOrLoss(netAmount)
    val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
    val table1             = buildTable1(profitOrLoss, 3000, 0.05, -3100)
    val table2             = buildTable2(profitOrLoss, 0, -0.05, 100.20)
    val table3             = buildTable3(profitOrLoss, 200, -200.1)
    // TODO SASS-8626 all of ^these^ hardcoded values will be replaced with API data
    Ok(
      view(
        request.userType,
        profitOrLoss,
        formattedNetAmount,
        table1,
        table2,
        table3,
        routes.CurrentYearLossesController.onPageLoad(taxYear, businessId, NormalMode)
      )
    ) // TODO if no losses this year go to PreviousUnusedLossesPage instead of CurrentYearLossesPage
  }

}
