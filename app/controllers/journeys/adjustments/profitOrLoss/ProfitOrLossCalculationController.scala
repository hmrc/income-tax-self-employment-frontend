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
import controllers.journeys
import models.NormalMode
import models.common._
import models.journeys.Journey.ProfitOrLoss
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.AdjustedTaxableProfitSummary._
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView

import javax.inject.{Inject, Singleton}

@Singleton
class ProfitOrLossCalculationController @Inject() (override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   view: ProfitOrLossCalculationView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val netAmount                 = BigDecimal(4600.00)
    val formattedNetAmount        = formatSumMoneyNoNegative(List(netAmount))
    val yourAdjustedProfitTable   = buildYourAdjustedProfitTable(taxYear)
    val netProfitTable            = buildNetProfitTable()
    val additionsToNetProfitTable = buildAdditionsToNetProfitTable()
    val capitalAllowanceTable     = buildCapitalAllowanceTable()
    val adjustmentsTable          = buildAdjustmentsTable()
    Ok(
      view(
        request.userType,
        formattedNetAmount,
        taxYear,
        yourAdjustedProfitTable,
        netProfitTable,
        additionsToNetProfitTable,
        capitalAllowanceTable,
        adjustmentsTable,
        journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss.entryName, NormalMode)
      ))
  }
}
