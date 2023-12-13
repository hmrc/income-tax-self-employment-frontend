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

import base.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.officeSupplies
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesOfficeSupplies
import pages.expenses.officeSupplies.OfficeSuppliesCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.officeSupplies.OfficeSuppliesAmountSummary
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

class OfficeSuppliesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = OfficeSuppliesCYAPage.toString

  private val officeSuppliesAmount = BigDecimal(200.00)

  override val journey: Journey = ExpensesOfficeSupplies

  def onPageLoadCall: (TaxYear, BusinessId) => Call = officeSupplies.routes.OfficeSuppliesCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = officeSupplies.routes.OfficeSuppliesCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(OfficeSuppliesAmountSummary.row(userAnswers, taxYear, businessId, userType.toString).value),
      classes = "govuk-!-margin-bottom-7"
    )

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {

    val view = application.injector.instanceOf[OfficeSuppliesCYAView]
    view(userType.toString, summaryList, taxYear, businessId)(request, messages).toString()
  }

  override val submissionData = Json.obj(
    "officeSupplies"       -> "yesAllowable",
    "officeSuppliesAmount" -> officeSuppliesAmount
  )
  override val testDataCases: List[JsObject] = List(submissionData)
}
