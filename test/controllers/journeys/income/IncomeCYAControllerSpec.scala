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

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerStubbedBaseSpec}
import cats.implicits.catsSyntaxEitherId
import controllers.journeys.income
import models.common.Journey.Income
import models.common._
import models.database.UserAnswers
import pages.income.IncomeCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.income._
import views.html.journeys.income.IncomeCYAView

class IncomeCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerStubbedBaseSpec {

  val turnoverIncomeAmount = BigDecimal(100)

  override def submissionData: JsObject = Json.obj(
    "incomeNotCountedAsTurnover" -> false,
    "turnoverIncomeAmount"       -> turnoverIncomeAmount,
    "anyOtherIncome"             -> false,
    "turnoverNotTaxable"         -> false,
    "tradingAllowance"           -> "declareExpenses"
  )

  override val testDataCases: List[JsObject] = List(submissionData)

  override def stubService =
    SelfEmploymentServiceStub(getTotalIncomeResult = turnoverIncomeAmount.asRight, clearSimplifiedExpensesDataResult = ().asRight)

  override val pageHeading: String = IncomeCYAPage.toString
  override val journey: Journey    = Income

  def onPageLoadCall: (TaxYear, BusinessId) => Call = income.routes.IncomeCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = income.routes.IncomeCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      IncomeNotCountedAsTurnoverSummary.row(userAnswers, taxYear, userType, businessId).value,
      TurnoverIncomeAmountSummary.row(userAnswers, taxYear, userType, businessId).value,
      AnyOtherIncomeSummary.row(userAnswers, taxYear, userType, businessId).value,
      TurnoverNotTaxableSummary.row(userAnswers, taxYear, userType, businessId).value,
      TradingAllowanceSummary.row(userAnswers, taxYear, userType, businessId).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {
    val view = application.injector.instanceOf[IncomeCYAView]
    view(taxYear, businessId, summaryList, userType)(request, messages).toString()
  }

}
