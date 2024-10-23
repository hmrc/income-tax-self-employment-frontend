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
import builders.BusinessIncomeSourcesSummaryBuilder.{aBusinessIncomeSourcesSummaryWithNetLoss, aBusinessIncomeSourcesSummaryWithNetProfit}
import builders.NetBusinessProfitOrLossValuesBuilder.{aNetBusinessLossValues, aNetBusinessProfitValues}
import builders.UserBuilder.aUserDateOfBirth
import common.TestApp.buildAppFromUserType
import controllers.journeys
import models.NormalMode
import models.common.Journey.ProfitOrLoss
import models.common.UserType
import models.common.UserType.Individual
import models.database.UserAnswers
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds
import models.journeys.nics.TaxableProfitAndLoss
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import utils.Assertions.assertEqualWithDiff
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary
import views.html.journeys.adjustments.profitOrLoss.ProfitOrLossCalculationView

import java.time.LocalDate

class ProfitOrLossCalculationControllerSpec extends ControllerSpec with TableDrivenPropertyChecks {

  def onPageLoadRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoad)

  def onPageLoad: String = routes.ProfitOrLossCalculationController.onPageLoad(taxYear, businessId).url

  def onwardRoute: Call = journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ProfitOrLoss, NormalMode)

  private val testScenarios = Table(
    ("profitOrLoss", "incomeSourceSummary", "netProfitOrLossValues"),
    (Profit, aBusinessIncomeSourcesSummaryWithNetProfit, aNetBusinessProfitValues),
    (Loss, aBusinessIncomeSourcesSummaryWithNetLoss, aNetBusinessLossValues),
  )

  "onPageLoad" - {
    "should return OK and render correct view" - {
      testScenarios.foreach { case (profitOrLoss, incomeSourceSummary, netProfitOrLossValues) =>
        val allTaxableProfitsAndLosses = List(TaxableProfitAndLoss(businessId, taxableProfit = incomeSourceSummary.taxableProfit, taxableLoss = incomeSourceSummary.taxableLoss))

        s"when net $profitOrLoss" in {
          val stubService = SelfEmploymentServiceStub(
            getBusinessIncomeSourcesSummaryResult = Right(incomeSourceSummary),
            getAllBusinessesTaxableProfitAndLossResult = Right(allTaxableProfitsAndLosses),
            getNetBusinessProfitOrLossValuesResult = Right(netProfitOrLossValues)
          )
          val netAmount = incomeSourceSummary.getNetBusinessProfitOrLossForTaxPurposes
          val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))

          val application = buildAppFromUserType(Individual, Some(emptyUserAnswers), Some(stubService))
          implicit val msg: Messages = SpecBase.messages(application)

          val result = route(application, onPageLoadRequest).value
          val adjustedProfitOrLoss = allTaxableProfitsAndLosses.headOption.get.taxableProfitOrLoss(profitOrLoss)
          val tables = AdjustedTaxableProfitOrLossSummary.buildTables(adjustedProfitOrLoss, netProfitOrLossValues, taxYear, profitOrLoss, Individual)

          val expectedView = {
            val view = application.injector.instanceOf[ProfitOrLossCalculationView]
            view(Individual, formattedNetAmount, taxYear, profitOrLoss, tables, None, onwardRoute)(onPageLoadRequest, msg).toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }
    }

    val tooYoungDoB = LocalDate.now().minusYears(15)
    val tooOldDoB   = LocalDate.now().minusYears(StatePensionAgeThresholds.getThresholdForTaxYear(taxYear) + 1)

    val class2TaxableProfit = 500
    val class4TaxableProfit = 12571
    val otherTaxableProfit = 10000
    def taxableProfitsAndLosses(taxableProfit: BigDecimal, taxableLoss: BigDecimal = 0): List[TaxableProfitAndLoss] = List(TaxableProfitAndLoss(businessId, taxableProfit = taxableProfit, taxableLoss = taxableLoss))

    val exemptionMessageCases = Table(
      ("scenario", "dateOfBirth", "taxableProfitsAndLosses", "expectedViewArguments"),
      ("'too young for Class 2' message when user is Class 2 eligible but under 16 years old", tooYoungDoB, taxableProfitsAndLosses(class2TaxableProfit), Some("class2Ineligible.tooYoung")),
//      ("'too old for Class 2' message when user is Class 2 eligible but over state pension age", tooOldDoB, taxableProfitsAndLosses(class2TaxableProfit), Some("class2Ineligible.tooOld")),
//      (
//        "'class 4 exemption due to age' message when user is Class 4 eligible but under 16 years old",
//        tooYoungDoB,
//        taxableProfitsAndLosses(class4TaxableProfit),
//        Some("class2Ineligible.tooYoung")),
//      (
//        "'class 4 exemption due to age' message when user is Class 4 eligible but over state pension age",
//        tooOldDoB,
//        taxableProfitsAndLosses(class4TaxableProfit),
//        Some("class2Ineligible.tooOld")),
//      (
//        "'user is between Class 2 and 4 thresholds' message when taxable profit is between thresholds",
//        aUserDateOfBirth,
//        taxableProfitsAndLosses(otherTaxableProfit),
//        Some("betweenClass2AndClass4"))
    )

    exemptionMessageCases.foreach { case (scenario, dateOfBirth, taxableProfitsAndLosses, expectedViewArguments) =>
      s"should display content with $scenario" in {
        val stubService = SelfEmploymentServiceStub(
          getBusinessIncomeSourcesSummaryResult = Right(aBusinessIncomeSourcesSummaryWithNetProfit),
          getAllBusinessesTaxableProfitAndLossResult = Right(taxableProfitsAndLosses),
          getNetBusinessProfitOrLossValuesResult = Right(aNetBusinessProfitValues),
          getUserDateOfBirthResult = Right(dateOfBirth)
        )

        val application            = buildAppFromUserType(UserType.Individual, Some(emptyUserAnswers), Some(stubService))
        val view                   = application.injector.instanceOf[ProfitOrLossCalculationView]
        implicit val msg: Messages = SpecBase.messages(application)

        val result             = route(application, onPageLoadRequest).value
        val netAmount          = aBusinessIncomeSourcesSummaryWithNetProfit.getNetBusinessProfitOrLossForTaxPurposes
        val formattedNetAmount = formatSumMoneyNoNegative(List(netAmount))
        val tables             = AdjustedTaxableProfitOrLossSummary.buildTables(netAmount, aNetBusinessProfitValues, taxYear, Profit, Individual)

        val expectedView =
          view(Individual, formattedNetAmount, taxYear, Profit, tables, expectedViewArguments, onwardRoute)(onPageLoadRequest, msg).toString()

        status(result) mustBe OK
        assert(contentAsString(result).contains(messages()))
        assertEqualWithDiff(contentAsString(result), expectedView)
      }
    }
  }
}
