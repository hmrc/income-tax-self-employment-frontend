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
import cats.implicits.catsSyntaxOptionId
import common.TestApp.buildAppFromUserType
import controllers.standard
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.adjustments.AdjustmentsPrepopAnswers
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import utils.Assertions.assertEqualWithDiff
import viewmodels.checkAnswers.prepop.AdjustmentsSummary.buildAdjustmentsTable
import views.html.journeys.prepop.AdjustmentsSummaryView

class AdjustmentsSummaryControllerSpec extends ControllerSpec {

  private val amount: BigDecimal = 2000
  def userAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "includedNonTaxableProfits"          -> amount,
      "accountingAdjustment"               -> amount,
      "averagingAdjustment"                -> amount,
      "outstandingBusinessIncome"          -> amount,
      "balancingChargeOther"               -> amount,
      "goodsAndServicesOwnUse"             -> amount,
      "transitionProfitAmount"             -> amount,
      "transitionProfitAccelerationAmount" -> amount
    ))
  def adjustmentAnswers: AdjustmentsPrepopAnswers =
    AdjustmentsPrepopAnswers(amount.some, amount.some, amount.some, amount.some, amount.some, amount.some, amount.some, amount.some)

  def onPageLoad: String = routes.AdjustmentsSummaryController.onPageLoad(taxYear, businessId).url

  def onPageLoadRequest = FakeRequest(GET, onPageLoad)

  "onPageLoad" - {
    "when answers for the user exist" - {
      List(Individual, Agent).foreach { userType =>
        s"should return Ok and render correct view when user is an $userType" in {
          val application  = buildAppFromUserType(userType, Some(userAnswers))
          implicit val msg = SpecBase.messages(application)
          val result       = route(application, onPageLoadRequest).value
          val expectedView: String = {
            val view = application.injector.instanceOf[AdjustmentsSummaryView]
            view(userType, taxYear, businessId, buildAdjustmentsTable(adjustmentAnswers))(onPageLoadRequest, msg).toString()
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
