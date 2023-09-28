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

  private val normalRoutes: Page => Int => UserAnswers => Call = {
    case CheckYourSelfEmploymentDetailsPage => taxYear => _ => controllers.journeys.routes.DetailsCompletedSectionController.onPageLoad(taxYear, TradeDetails.toString, NormalMode)
    case SelfEmploymentAbroadPage => taxYear => _ => controllers.journeys.routes.DetailsCompletedSectionController.onPageLoad(taxYear, Abroad.toString, NormalMode)
    case DetailsCompletedSectionPage => taxYear => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)
    case _ => taxYear => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case _ => taxYear => _ =>  controllers.standard.routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, taxYear: Int, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
