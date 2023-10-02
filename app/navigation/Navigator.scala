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

import controllers.routes._
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => Int => UserAnswers => Call = {
    case CheckYourSelfEmploymentDetailsPage => taxYear => _ => DetailsCompletedSectionController.onPageLoad(taxYear, TradeDetails.toString, NormalMode) //TODO direct to CYA page when created
    case SelfEmploymentAbroadPage => taxYear => _ => DetailsCompletedSectionController.onPageLoad(taxYear, Abroad.toString, NormalMode) //TODO direct to CYA page when created
    case DetailsCompletedSectionPage => taxYear => _ => TaskListController.onPageLoad(taxYear)
    case _ => taxYear => _ => TaskListController.onPageLoad(taxYear)
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case _ => taxYear => _ => CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, taxYear: Int, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
