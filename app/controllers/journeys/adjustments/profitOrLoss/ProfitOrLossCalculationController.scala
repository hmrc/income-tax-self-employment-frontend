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
import controllers.{handleResultT, journeys}
import models.NormalMode
import models.common.Journey.ProfitOrLoss
import models.common._
import models.domain.{ApiResultT, BusinessIncomeSourcesSummary}
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds.ageIsBetween16AndStatePension
import models.journeys.nics.TaxableProfitAndLoss
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatSumMoneyNoNegative
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView
import services.SelfEmploymentService
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProfitOrLossCalculationController @Inject() (override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   service: SelfEmploymentService,
                                                   view: ProfitOrLossCalculationView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val result = for {
        incomeSummary <- service.getBusinessIncomeSourcesSummary(taxYear, request.nino, businessId, request.mtditid)
        netAmount          = incomeSummary.getNetBusinessProfitForTaxPurposes()
        profitOrLoss       = incomeSummary.returnProfitOrLoss()
        formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
        tables             = AdjustedTaxableProfitOrLossSummary.buildTables(taxYear, profitOrLoss)
        showClass4AgeExemptionMessage <- showClass4AgeExemption(taxYear, incomeSummary)
      } yield Ok(
        view(
          request.userType,
          formattedNetAmount,
          taxYear,
          profitOrLoss,
          tables,
          showClass4AgeExemptionMessage,
          journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss, NormalMode)
        )
      )
      handleResultT(result)
  }
  private def showClass4AgeExemption(taxYear: TaxYear, incomeSummary: BusinessIncomeSourcesSummary)(implicit
      request: DataRequest[_]): ApiResultT[Boolean] =
    service.getUserDateOfBirth(request.nino, request.mtditid).map { userDoB =>
      val profitsOverClass4Threshold = TaxableProfitAndLoss
        .areProfitsOverClass4Threshold(List(TaxableProfitAndLoss(incomeSummary.taxableProfit, incomeSummary.taxableLoss)), taxYear)
      val ageIsValid = ageIsBetween16AndStatePension(userDoB, taxYear, ageAtStartOfTaxYear = true)
      profitsOverClass4Threshold && !ageIsValid
    }
}
