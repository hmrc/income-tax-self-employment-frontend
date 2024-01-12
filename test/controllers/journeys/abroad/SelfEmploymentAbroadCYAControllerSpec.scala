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

package controllers.journeys.abroad

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.abroad.SelfEmploymentAbroadSummary
import viewmodels.journeys.SummaryListCYA

class SelfEmploymentAbroadCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {
  val pageHeading: String           = "common.checkYourDetailsa"
  val journey: Journey              = Journey.Abroad
  val submissionData: JsObject      = Json.obj("selfEmploymentAbroad" -> true)
  val testDataCases: List[JsObject] = List(submissionData)

  def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.SelfEmploymentAbroadCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.SelfEmploymentAbroadCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    SummaryListCYA.summaryListOpt(
      List(
        SelfEmploymentAbroadSummary.row(taxYear, userType, businessId, userAnswers)
      ))

}
