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

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import builders.ExpensesTailoringJsonBuilder._
import controllers.journeys.expenses.tailoring
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.common.Journey
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import pages.expenses.tailoring._
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.tailoring.buildTailoringSummaryList
import views.html.standard.CheckYourAnswersView

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = ExpensesTailoringCYAPage.toString
  override val journey: Journey    = Journey.ExpensesTailoring

  def onPageLoadCall: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad

  def onSubmitCall: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    buildTailoringSummaryList(userAnswers, taxYear, businessId, userType)

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {
    val view         = application.injector.instanceOf[CheckYourAnswersView]
    val categoryText = summaryList.rows.head.value.content
    val optCategory  = if (categoryText == HtmlContent(messages(s"expenses.$IndividualCategories"))) "Categories" else ""
    val heading      = s"$pageHeading$optCategory"
    view(heading, taxYear, userType, summaryList, onSubmitCall(taxYear, businessId))(request, messages).toString()
  }

  override lazy val submissionData: JsObject = allYesIndividualCategoriesAnswers

  override lazy val testDataCases: List[JsObject] =
    List(
      allYesIndividualCategoriesAnswers,
      allNoIndividualCategoriesAnswers,
      mixedIndividualCategoriesAnswers,
      noExpensesAnswers,
      totalAmountAnswers
    )

}
