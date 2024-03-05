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

package controllers.journeys.expenses.workplaceRunningCosts

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesWorkplaceRunningCosts
import pages.expenses.workplaceRunningCosts.WorkplaceRunningCostsCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.workplaceRunningCosts._
import views.html.standard.CheckYourAnswersView

class WorkplaceRunningCostsCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = WorkplaceRunningCostsCYAPage.toString
  override val journey: Journey    = ExpensesWorkplaceRunningCosts

  def onPageLoadCall: (TaxYear, BusinessId) => Call = expenses.workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = expenses.workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = Seq(
        MoreThan25HoursSummary.row(userAnswers, taxYear, businessId, userType).value,
        WorkingFromHome25To50HoursSummary.row(userAnswers, taxYear, businessId, userType).value,
        WorkingFromHome51To100HoursSummary.row(userAnswers, taxYear, businessId, userType).value,
        WorkingFromHome101PlusHoursSummary.row(userAnswers, taxYear, businessId, userType).value,
        WfhFlatRateOrActualCostsSummary.row(userAnswers, taxYear, businessId, userType).value,
        WfhClaimingAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        LiveAtBusinessPremisesSummary.row(userAnswers, taxYear, businessId, userType).value,
        BusinessPremisesAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        BusinessPremisesDisallowableAmountSummary.row(userAnswers, taxYear, businessId, userType).value,
        LivingAtBusinessPremisesOnePersonSummary.row(userAnswers, taxYear, businessId, userType).value,
        LivingAtBusinessPremisesTwoPeopleSummary.row(userAnswers, taxYear, businessId, userType).value,
        LivingAtBusinessPremisesThreePlusPeopleSummary.row(userAnswers, taxYear, businessId, userType).value,
        WfbpFlatRateOrActualCostsSummary.row(userAnswers, taxYear, businessId, userType).value,
        WfbpClaimingAmountSummary.row(userAnswers, taxYear, businessId, userType).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {
    val view = application.injector.instanceOf[CheckYourAnswersView]
    view(pageHeading, taxYear, userType, summaryList, onSubmitCall(taxYear, businessId))(request, messages).toString()
  }

  override val submissionData: JsObject = Json.obj(
    "moreThan25Hours"                         -> true,
    "wfhHours25To50"                          -> 1,
    "wfhHours51To100"                         -> 1,
    "wfhHours101Plus"                         -> 1,
    "wfhFlatRateOrActualCosts"                -> "flatRate",
    "wfhClaimingAmount"                       -> 100,
    "liveAtBusinessPremises"                  -> true,
    "businessPremisesAmount"                  -> 100,
    "businessPremisesDisallowableAmount"      -> 100,
    "livingAtBusinessPremisesOnePerson"       -> 1,
    "livingAtBusinessPremisesTwoPeople"       -> 2,
    "livingAtBusinessPremisesThreePlusPeople" -> 3.00,
    "wfbpFlatRateOrActualCosts"               -> "flatRate",
    "wfbpClaimingAmount"                      -> 100.00
  )

  override val testDataCases: List[JsObject] = List(submissionData)

}
