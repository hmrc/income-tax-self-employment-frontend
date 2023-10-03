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

  private val normalRoutes: Page => UserAnswers => (Int, Option[String]) => Call = {
    case CheckYourSelfEmploymentDetailsPage => _ => (taxYear, businessId) =>
    DetailsCompletedSectionController.onPageLoad(taxYear, TradeDetails.toString, NormalMode) //TODO direct to reviewSummary page when created

    case SelfEmploymentAbroadPage => _ => (taxYear, businessId) =>
      DetailsCompletedSectionController.onPageLoad(taxYear, Abroad.toString, NormalMode) //TODO direct to SelfEmploymentAbroad CYA page when created

    case DetailsCompletedSectionPage => _ => (taxYear, _) => TaskListController.onPageLoad(taxYear)
    case _ => _ => (taxYear, _) => TaskListController.onPageLoad(taxYear)
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case _ => taxYear => _ => CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: Int, businessId: Option[String] = None): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)(taxYear, businessId)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
