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

import base.CYAControllerBaseSpec
import models.common.{UserType, onwardRoute}
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.officeSupplies.OfficeSuppliesAmountSummary
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

class OfficeSuppliesCYAControllerSpec extends CYAControllerBaseSpec("OfficeSuppliesCYAController") {

  private val userAnswerData = Json
    .parse(s"""
         |{
         |  "$stubbedBusinessId": {
         |    "officeSupplies": "yesAllowable",
         |    "officeSuppliesAmount": 200.00
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  override protected val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)

  override protected lazy val onPageLoadRoute: String = routes.OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId).url

  override val bindings: List[Binding[_]] = List(bind[ExpensesNavigator].to(new FakeExpensesNavigator(onwardRoute)))

  override def expectedSummaryList(authUserType: UserType)(implicit messages: Messages): SummaryList =
    SummaryList(
      rows = List(OfficeSuppliesAmountSummary.row(userAnswers, taxYear, stubbedBusinessId, authUserType.toString).value),
      classes = "govuk-!-margin-bottom-7"
    )

  override def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {

    val view = application.injector.instanceOf[OfficeSuppliesCYAView]
    view(scenario.userType.toString, summaryList, taxYear, nextRoute).toString()
  }

}
