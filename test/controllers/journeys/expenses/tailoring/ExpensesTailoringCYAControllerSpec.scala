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

package controllers.journeys.expenses.tailoring

import base.CYAOnPageLoadControllerSpec
import builders.ExpensesTailoringJsonBuilder._
import controllers.journeys.expenses.tailoring
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.tailoring.buildTailoringSummaryList

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerSpec {

  override val pageName: String = s"${ExpensesTailoringCYAPage.toString}Categories"

  def onPageLoadCall: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = tailoring.routes.ExpensesTailoringCYAController.onSubmit

  def getSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    buildTailoringSummaryList(userAnswers, taxYear, businessId, userType)

  override val testDataCases: List[JsObject] =
    List(
      allYesAnswers,
      allNoAnswers,
      mixedAnswers
    )

}
