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

package navigation

import controllers.journeys.routes._
import controllers.journeys.tradeDetails.routes.SelfEmploymentSummaryController
import controllers.standard.routes._
import models._
import models.database.UserAnswers
import models.journeys.Journey.TradeDetails
import pages._
import pages.tradeDetails.{CheckYourSelfEmploymentDetailsPage, SelfEmploymentSummaryPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class TradeDetailsNavigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => (Int, String) => Call = {

    case CheckYourSelfEmploymentDetailsPage => _ => (taxYear, _) => SelfEmploymentSummaryController.onPageLoad(taxYear)

    case SelfEmploymentSummaryPage =>
      _ => (taxYear, businessId) => SectionCompletedStateController.onPageLoad(taxYear, businessId, TradeDetails.toString, NormalMode)

    case _ => _ => (_, _) => JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => (Int, String) => Call = { case _ =>
    _ => (_, _) => JourneyRecoveryController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: String): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)(taxYear, businessId)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)(taxYear, businessId)
  }

}
