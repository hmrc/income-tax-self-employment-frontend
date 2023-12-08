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

package controllers.journeys.expenses.repairsandmaintenance

import base.{CYAOnPageLoadControllerSpec, CYAOnSubmitControllerBaseSpec}
import builders.UserBuilder.{aNoddyAgentUser, aNoddyUser}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.ExpensesRepairsAndMaintenance
import models.requests.DataRequest
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountSummary, RepairsAndMaintenanceDisallowableAmountSummary}

class RepairsAndMaintenanceCostsCYAControllerSpecII extends CYAOnPageLoadControllerSpec with CYAOnSubmitControllerBaseSpec {

  override def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.RepairsAndMaintenanceCostsCYAController.onPageLoad
  override def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.RepairsAndMaintenanceCostsCYAController.onSubmit

  // TODO Align the base traits better. CYAOnPageLoadControllerSpec & CYAOnSubmitControllerBaseSpec generate user
  //  answers differently - leads to duplication.
  private val data = Json
    .parse(s"""
              |{
              |  "$businessId": {
              |    "repairsAndMaintenance": "yesDisallowable",
              |    "repairsAndMaintenanceAmount": 200.00,
              |    "repairsAndMaintenanceDisallowableAmount": 200.00
              |  }
              |}
              |""".stripMargin)
    .as[JsObject]

  override val userAnswers: UserAnswers = UserAnswers(userAnswersId, data)

  override val journey: Journey = ExpensesRepairsAndMaintenance

  override val pageHeading: String = RepairsAndMaintenanceCostsCYAPage.toString

  override val testDataCases: List[JsObject] = List(
    Json.obj(
      "repairsAndMaintenance"                   -> "yesDisallowable",
      "repairsAndMaintenanceAmount"             -> 200.00,
      "repairsAndMaintenanceDisallowableAmount" -> 200.00
    ))

  override def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryList(
      rows = List(
        RepairsAndMaintenanceAmountSummary.row(dataRequestForUser(userType), taxYear, businessId).value,
        RepairsAndMaintenanceDisallowableAmountSummary.row(dataRequestForUser(userType), taxYear, businessId).value
      ),
      classes = "govuk-!-margin-bottom-7"
    )

  private def dataRequestForUser(userType: UserType) =
    userType match {
      case UserType.Individual => DataRequest(postRequest, userAnswersId, aNoddyUser, userAnswers)
      case UserType.Agent      => DataRequest(postRequest, userAnswersId, aNoddyAgentUser, userAnswers)
    }

}
