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
import models.domain.ApiResultT
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds.{ageIsBetween16AndStatePension, ageIsUnder16, ageIsUnderStatePensionAge}
import models.journeys.nics.TaxableProfitAndLoss
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary.buildSummaryLists
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView

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
      val onwardRoute = journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss, NormalMode)
      val result = for {
        taxableProfitsAndLosses <- service.getAllBusinessesTaxableProfitAndLoss(taxYear, request.nino, request.mtditid)
        netProfitOrLossValues   <- service.getNetBusinessProfitOrLossValues(taxYear, request.nino, businessId, request.mtditid)
        incomeSummary           <- service.getBusinessIncomeSourcesSummary(taxYear, request.nino, businessId, request.mtditid)
        journeyIsProfitOrLoss = incomeSummary.journeyIsNetProfitOrLoss
        adjustedTaxablePoL    = incomeSummary.getTaxableProfitOrLossAmount
        netPoLForTaxPurposes  = incomeSummary.getNetBusinessProfitOrLossForTaxPurposes
        summaryLists          = buildSummaryLists(adjustedTaxablePoL, netProfitOrLossValues, taxYear, journeyIsProfitOrLoss, request.userType)
        taxableProfitWhenProfitAndLossDeclared =
          if (incomeSummary.taxableProfit > 0 && incomeSummary.taxableLoss > 0) Some(incomeSummary.taxableProfit) else None
        nicsExemptionMessage <- showNicsExemptionMessage(taxYear, taxableProfitsAndLosses)
      } yield Ok(view(
        request.userType,
        journeyIsProfitOrLoss,
        adjustedTaxablePoL,
        netPoLForTaxPurposes,
        taxYear,
        summaryLists,
        taxableProfitWhenProfitAndLossDeclared,
        nicsExemptionMessage,
        onwardRoute
      ))

      handleResultT(result)
  }

  private def showNicsExemptionMessage(taxYear: TaxYear, taxableProfitsAndLosses: List[TaxableProfitAndLoss])(implicit
      request: DataRequest[_]): ApiResultT[Option[String]] =
    service.getUserDateOfBirth(request.nino, request.mtditid, request.user.dateOfBirth, request.userType == UserType.Agent).map { userDoB =>
      val userIsClass2Eligible = TaxableProfitAndLoss.areProfitsOrLossClass2Eligible(taxableProfitsAndLosses, taxYear)
      val userIsClass4Eligible = TaxableProfitAndLoss.areProfitsOverClass4Threshold(taxableProfitsAndLosses, taxYear)
      val class2AgeIsTooYoung  = userDoB.map(ageIsUnder16(_, taxYear, ageAtStartOfTaxYear = false))
      val class2AgeIsTooOld    = userDoB.map(!ageIsUnderStatePensionAge(_, taxYear, ageAtStartOfTaxYear = false))
      val class4AgeIsInvalid   = userDoB.map(!ageIsBetween16AndStatePension(_, taxYear, ageAtStartOfTaxYear = true))

      (userIsClass2Eligible, userIsClass4Eligible, class2AgeIsTooYoung, class2AgeIsTooOld) match {
        case (_, true, _, _) if class4AgeIsInvalid.contains(true) => // User can be Class 2 and 4 eligible -> 4 takes priority
          Some("class4Ineligible.tooOldOrYoung")
        case (true, false, Some(true), Some(_)) => Some("class2Ineligible.tooYoung")
        case (true, false, Some(_), Some(true)) => Some("class2Ineligible.tooOld")
        case (false, false, _, _)               => Some("betweenClass2AndClass4")
        case _                                  => None
      }
    }
}
