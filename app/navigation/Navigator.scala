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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => (Int, Option[String]) => UserAnswers => Call = {
    case CheckYourSelfEmploymentDetailsPage => (taxYear, _) => _ =>
      controllers.journeys.tradeDetails.routes.SelfEmploymentSummaryController.onPageLoad(taxYear)

    case SelfEmploymentSummaryPage => (taxYear, optBusinessId) => _ =>
      controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, optBusinessId.getOrElse(""), TradeDetails.toString, NormalMode)

    case SelfEmploymentAbroadPage => (taxYear, optBusinessId) => _ =>
      controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, optBusinessId.getOrElse(""), Abroad.toString, NormalMode)

    case SectionCompletedStatePage => (taxYear, _) => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)

    case _ => (taxYear, _) => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case _ => taxYear => _ =>  controllers.standard.routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, optBusinessId: Option[String] = None): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear, optBusinessId)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
