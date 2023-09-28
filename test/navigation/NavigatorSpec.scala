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
import controllers.routes
import models._
import pages._

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val taxYear = LocalDate.now().getYear

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        navigator.nextPage(UnknownPage, NormalMode, taxYear, UserAnswers("id")) mustBe routes.TaskListController.onPageLoad(taxYear)
      }

      "must go from the last page in a journey to the 'Have you completed this section?' page" in {

        navigator.nextPage(SelfEmploymentAbroadPage, NormalMode, taxYear, UserAnswers("id")) mustBe routes.TaskListController.onPageLoad(taxYear)
        //TODO check redirect to DetailsCompleted page when implemented
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        navigator.nextPage(UnknownPage, CheckMode, taxYear, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
