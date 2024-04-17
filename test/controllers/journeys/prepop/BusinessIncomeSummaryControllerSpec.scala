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

package controllers.journeys.prepop

import base.{ControllerSpec, SpecBase}
import common.TestApp.buildAppFromUserType
import controllers.standard
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.income.IncomePrepopAnswers
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import utils.Assertions.assertEqualWithDiff
import viewmodels.checkAnswers.buildTable
import viewmodels.checkAnswers.prepop.PrepopIncomeSummary.{headRow, otherIncomeRow, totalIncomeRow, turnoverIncomeRow}
import views.html.journeys.prepop.BusinessIncomeSummaryView

class BusinessIncomeSummaryControllerSpec extends ControllerSpec {

  def userAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "turnoverIncome" -> BigDecimal(2000),
      "otherIncome"    -> BigDecimal(1000)
    ))
  def incomeAnswers: IncomePrepopAnswers = IncomePrepopAnswers(Some(2000), Some(1000))

  def onPageLoad: String = routes.BusinessIncomeSummaryController.onPageLoad(taxYear, businessId).url

  def onPageLoadRequest = FakeRequest(GET, onPageLoad)

  "onPageLoad" - {
    "when answers for the user exist" - {
      List(Individual, Agent).foreach { userType =>
        s"should return Ok and render correct view when user is an $userType" in {
          val application  = buildAppFromUserType(userType, Some(userAnswers))
          implicit val msg = SpecBase.messages(application)
          val result       = route(application, onPageLoadRequest).value
          val incomeTable: Table = buildTable(
            headRow,
            Seq(
              turnoverIncomeRow(incomeAnswers),
              otherIncomeRow(incomeAnswers),
              totalIncomeRow(incomeAnswers)
            ).flatten)
          val expectedView: String = {
            val view = application.injector.instanceOf[BusinessIncomeSummaryView]
            view(userType, taxYear, businessId, incomeTable)(onPageLoadRequest, msg).toString()
          }

          status(result) mustBe OK
          assertEqualWithDiff(contentAsString(result), expectedView)
        }
      }
    }
    "when no user answers exist" - {
      "should redirect to the journey recovery controller" in {
        val application = buildAppFromUserType(Individual)
        val result      = route(application, onPageLoadRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
