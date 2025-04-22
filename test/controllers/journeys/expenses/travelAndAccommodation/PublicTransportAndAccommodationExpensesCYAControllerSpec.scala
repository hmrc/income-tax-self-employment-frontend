/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.travelAndAccommodation

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.travelAndAccommodation.PublicTransportAndAccommodationExpensesCYAPage
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.travelAndAccommodation.PublicTransportAndAccommodationExpensesSummary
class PublicTransportAndAccommodationExpensesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = PublicTransportAndAccommodationExpensesCYAPage.toString

  override val journey: Journey = ExpensesTravelForWork

  def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.PublicTransportAndAccommodationExpensesCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.PublicTransportAndAccommodationExpensesCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    PublicTransportAndAccommodationExpensesSummary(userAnswers, taxYear, businessId, userType)

  val data: String =
    s"""
       |{
       |    "SJPR05893938001": {
       |        "travelForWork": "yesDisallowable",
       |        "travelAndAccommodationExpenseType": [
       |            "publicTransportAndOtherAccommodation"
       |        ],
       |        "publicTransportAndAccommodationExpenses": 198.75,
       |        "disallowableTransportAndAccommodation": 15.45
       |    }
       |}
       |""".stripMargin

  override val submissionData: JsObject      = Json.parse(data).asInstanceOf[JsObject]
  override val testDataCases: List[JsObject] = List(submissionData)

}
