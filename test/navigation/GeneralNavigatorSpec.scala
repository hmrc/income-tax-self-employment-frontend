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
import controllers.journeys.routes._
import controllers.standard.routes._
import pages._

class GeneralNavigatorSpec extends SpecBase {

  val navigator = new GeneralNavigator

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "must go from a Section Completed page to the Task List page" in {

        navigator.nextPage(SectionCompletedStatePage, taxYear) mustBe TaskListController
          .onPageLoad(taxYear)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, taxYear) mustBe JourneyRecoveryController.onPageLoad()
      }
    }
  }

}
