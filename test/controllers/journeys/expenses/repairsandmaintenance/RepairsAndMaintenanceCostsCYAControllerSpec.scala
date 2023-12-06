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

import base.SpecBase._
import builders.UserBuilder.aNoddyUser
import common.TestApp.buildAppFromUserAnswers
import models.common.UserType
import models.database.UserAnswers
import models.requests.DataRequest
import models.test.RepairsAndMaintenanceInfo
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountSummary, RepairsAndMaintenanceDisallowableAmountSummary}
import views.html.journeys.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAView

import scala.concurrent.Future

class RepairsAndMaintenanceCostsCYAControllerSpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {

  // Kept these UTs as may want to use this approach of Table-based testing for different answers.
  private def createUserAnswerData(info: RepairsAndMaintenanceInfo) = Json
    .parse(s"""
         |{
         |  "$businessId": ${Json.toJson(info)}
         |}
         |""".stripMargin)
    .as[JsObject]

  lazy val routeUnderTest = routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url
  lazy val getRequest     = FakeRequest(GET, routeUnderTest)

  "onPageLoad" should {
    val cases = Table(
      ("tailoring", "amount", "disallowable"),
      ("yesAllowable", Some(BigDecimal(100.0)), None),
      ("yesDisallowable", Some(BigDecimal(100.00)), None),
      ("yesDisallowable", Some(BigDecimal(100.00)), Some(BigDecimal(50.00)))
    )

    forAll(cases) { case (tailoring, amount, disallowable) =>
      s"return OK and render view for $tailoring, amount=$amount, and disallowable=$disallowable" in {
        val existingData                       = RepairsAndMaintenanceInfo(Some(tailoring), amount, disallowable)
        val (application, userAnswers, result) = callRoute(existingData)

        status(result) mustBe OK

        implicit val msg: Messages = messages(application)
        val dataRequest            = DataRequest(getRequest, userAnswersId, aNoddyUser, userAnswers)
        val expectedRows = List(
          RepairsAndMaintenanceAmountSummary.row(dataRequest, taxYear, businessId),
          RepairsAndMaintenanceDisallowableAmountSummary.row(dataRequest, taxYear, businessId)
        ).flatten
        contentAsString(result) mustEqual createExpectedView(application, expectedRows)
      }
    }
  }

  def callRoute(existingData: RepairsAndMaintenanceInfo): (Application, UserAnswers, Future[Result]) = {
    val userAnswersData = createUserAnswerData(existingData)
    val userAnswers     = UserAnswers(userAnswersId, userAnswersData)
    val application     = buildAppFromUserAnswers(userAnswers)

    (application, userAnswers, route(application, getRequest).value)
  }

  def createExpectedView(application: Application, expectedRows: List[SummaryListRow])(implicit msg: Messages): String = {
    val view: RepairsAndMaintenanceCostsCYAView = application.injector.instanceOf[RepairsAndMaintenanceCostsCYAView]
    val summaryList                             = SummaryList(rows = expectedRows, classes = "govuk-!-margin-bottom-7")
    val onSubmitCall                            = routes.RepairsAndMaintenanceCostsCYAController.onSubmit(taxYear, businessId)

    view(RepairsAndMaintenanceCostsCYAPage.pageName, taxYear, summaryList, UserType.Individual, onSubmitCall)(getRequest, msg).toString()
  }

}
