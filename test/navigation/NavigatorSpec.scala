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

import base.SpecBase
import controllers.journeys.tradeDetails.{routes => tdRoutes}
import controllers.journeys.{routes => jRoutes}
import controllers.standard.{routes => stRoutes}
import models._
import pages._

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val taxYear = LocalDate.now().getYear

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "must go from the Check Your Self Employment Details page to the 'Have you completed this section?' page" in {

        navigator.nextPage(CheckYourSelfEmploymentDetailsPage, NormalMode, UserAnswers("id"), taxYear) mustBe
          tdRoutes.SelfEmploymentSummaryController.onPageLoad(taxYear)
      }

      "must go from the Self-employment Abroad page to the 'Have you completed this section?' page" in {

        navigator.nextPage(SelfEmploymentAbroadPage, NormalMode, UserAnswers("id"), taxYear) mustBe
          jRoutes.SectionCompletedStateController.onPageLoad(taxYear, Abroad.toString, NormalMode)
      }

      "must go from a Details Completed page to the Task List page" in {

        navigator.nextPage(SectionCompletedStatePage, NormalMode, UserAnswers("id"), taxYear) mustBe jRoutes.TaskListController.onPageLoad(taxYear)
      }

      "must go from a page that doesn't exist in the route map to Index" in {

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"), taxYear) mustBe jRoutes.TaskListController.onPageLoad(taxYear)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"), taxYear) mustBe stRoutes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
