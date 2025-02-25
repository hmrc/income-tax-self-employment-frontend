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

import base.{ControllerSpec, SpecBase}
import builders.BusinessIncomeSourcesSummaryBuilder.{
  aBusinessIncomeSourcesSummary,
  aBusinessIncomeSourcesSummaryWithNetLoss,
  aBusinessIncomeSourcesSummaryWithNetProfit
}
import builders.NetBusinessProfitOrLossValuesBuilder.{aNetBusinessLossValues, aNetBusinessProfitValues}
import builders.UserBuilder.aUserDateOfBirth
import common.TestApp.buildAppFromUserType
import controllers.journeys
import models.NormalMode
import models.common.Journey.ProfitOrLoss
import models.common.UserType
import models.common.UserType.Individual
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds
import models.journeys.nics.TaxableProfitAndLoss
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import utils.Assertions.assertEqualWithDiff
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView

class ProfitOrLossCalculationControllerSpec extends ControllerSpec with TableDrivenPropertyChecks {

  def onPageLoadRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoad)

  def onPageLoad: String = routes.ProfitOrLossCalculationController.onPageLoad(taxYear, businessId).url

  def onwardRoute: Call = journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss, NormalMode)

  private val profitOrLossScenarios = Table(
    ("profitOrLoss", "incomeSourceSummary", "netProfitOrLossValues"),
    (Profit, aBusinessIncomeSourcesSummaryWithNetProfit, aNetBusinessProfitValues),
    (Loss, aBusinessIncomeSourcesSummaryWithNetLoss, aNetBusinessLossValues)
  )

  private val tooYoungDoB = mockTimeMachine.now.minusYears(15)
  private val tooOldDoB   = mockTimeMachine.now.minusYears(StatePensionAgeThresholds.getThresholdForTaxYear(taxYear) + 1)

  private val class2TaxableProfit = 500
  private val class4TaxableProfit = 12571
  private val otherTaxableProfit  = 10000

  private val class2TooYoungMsg = "As you’re under 16 years of age, you cannot make voluntary Class 2 National Insurance contributions."
  private val class2TooOldMsg =
    "You cannot make voluntary Class 2 National Insurance Contributions for any weeks after you’ve reached State Pension age."
  private val class4InvalidAgeMsg = "Based on your age, you’re exempt and therefore do not need to pay Class 4 National Insurance contributions."
  private val betweenThresholdsMsg =
    "Based on the figures above, your National Insurance Class 2 contributions are treated as having been paid and you do not need to pay Class 4 contributions."

  def taxableProfitsAndLosses(taxableProfit: BigDecimal, taxableLoss: BigDecimal = 0): List[TaxableProfitAndLoss] = List(
    TaxableProfitAndLoss(businessId, taxableProfit = taxableProfit, taxableLoss = taxableLoss))

  private val exemptionMessageScenarios = Table(
    ("scenario", "dateOfBirth", "taxableProfitsAndLosses", "expectedNicsMessage"),
    (
      "'too young for Class 2' message when user is Class 2 eligible but under 16 years old",
      tooYoungDoB,
      taxableProfitsAndLosses(class2TaxableProfit),
      class2TooYoungMsg),
    (
      "'too old for Class 2' message when user is Class 2 eligible but over state pension age",
      tooOldDoB,
      taxableProfitsAndLosses(class2TaxableProfit),
      class2TooOldMsg),
    (
      "'class 4 exemption due to age' message when user is Class 4 eligible but under 16 years old",
      tooYoungDoB,
      taxableProfitsAndLosses(class4TaxableProfit),
      class4InvalidAgeMsg),
    (
      "'class 4 exemption due to age' message when user is Class 4 eligible but over state pension age",
      tooOldDoB,
      taxableProfitsAndLosses(class4TaxableProfit),
      class4InvalidAgeMsg),
    (
      "'user is between Class 2 and 4 thresholds' message when taxable profit is between thresholds",
      aUserDateOfBirth,
      taxableProfitsAndLosses(otherTaxableProfit),
      betweenThresholdsMsg)
  )

  "onPageLoad" - {
    "should return OK and render correct view" - {
      profitOrLossScenarios.foreach { case (profitOrLoss, incomeSourceSummary, netProfitOrLossValues) =>
        val allTaxableProfitsAndLosses =
          List(TaxableProfitAndLoss(businessId, taxableProfit = incomeSourceSummary.taxableProfit, taxableLoss = incomeSourceSummary.taxableLoss))

        s"when net $profitOrLoss" in {
          val stubService = SelfEmploymentServiceStub(
            getBusinessIncomeSourcesSummaryResult = Right(incomeSourceSummary),
            getAllBusinessesTaxableProfitAndLossResult = Right(allTaxableProfitsAndLosses),
            getNetBusinessProfitOrLossValuesResult = Right(netProfitOrLossValues)
          )
          val adjustedTaxablePoL   = incomeSourceSummary.getTaxableProfitOrLossAmount
          val netPoLForTaxPurposes = incomeSourceSummary.getNetBusinessProfitOrLossForTaxPurposes
          val taxableProfitWhenProfitAndLossDeclared =
            if (incomeSourceSummary.taxableProfit > 0 && incomeSourceSummary.taxableLoss > 0)
              Some(incomeSourceSummary.taxableProfit)
            else None

          val application            = buildAppFromUserType(Individual, Some(emptyUserAnswers), Some(stubService))
          implicit val msg: Messages = SpecBase.messages(application)

          val result = route(application, onPageLoadRequest).value
          val adjustedProfitOrLoss: BigDecimal = {
            val taxableProfitAndLoss = allTaxableProfitsAndLosses.headOption.get
            if (profitOrLoss == Profit) taxableProfitAndLoss.taxableProfit else -taxableProfitAndLoss.taxableLoss
          }
          val tables =
            AdjustedTaxableProfitOrLossSummary.buildSummaryLists(adjustedProfitOrLoss, netProfitOrLossValues, taxYear, profitOrLoss, Individual)

          val expectedView = {
            val view = application.injector.instanceOf[ProfitOrLossCalculationView]
            view(
              Individual,
              profitOrLoss,
              adjustedTaxablePoL,
              netPoLForTaxPurposes,
              taxYear,
              tables,
              taxableProfitWhenProfitAndLossDeclared,
              None,
              onwardRoute
            )(onPageLoadRequest, msg).toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }

      "that displays the notification:" - {
        exemptionMessageScenarios.foreach { case (scenario, dateOfBirth, taxableProfitsAndLosses, expectedNicsMessage) =>
          scenario in {
            val stubService = SelfEmploymentServiceStub(
              getBusinessIncomeSourcesSummaryResult = Right(aBusinessIncomeSourcesSummaryWithNetProfit),
              getAllBusinessesTaxableProfitAndLossResult = Right(taxableProfitsAndLosses),
              getNetBusinessProfitOrLossValuesResult = Right(aNetBusinessProfitValues),
              getUserDateOfBirthResult = Right(dateOfBirth)
            )

            val application = buildAppFromUserType(UserType.Individual, Some(emptyUserAnswers), Some(stubService))

            val result = route(application, onPageLoadRequest).value

            assert(contentAsString(result) contains expectedNicsMessage)
          }
        }
      }

      "should display the taxable profit when both profit and loss declared" in {

        val stubService = SelfEmploymentServiceStub(
          getBusinessIncomeSourcesSummaryResult = Right(aBusinessIncomeSourcesSummary)
        )

        val application = buildAppFromUserType(Individual, Some(emptyUserAnswers), Some(stubService))

        val result = route(application, onPageLoadRequest).value

        val expectedMessage =
          s"You also have a taxable profit of £${aBusinessIncomeSourcesSummary.taxableProfit}, which is separate to the loss above. You may be able to offset this loss against other types of taxable income or carry it forward to a future tax year."

        assert(contentAsString(result).contains(expectedMessage))
      }
    }
  }
}
