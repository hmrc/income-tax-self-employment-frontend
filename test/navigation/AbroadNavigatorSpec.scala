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
import controllers.journeys.abroad.routes._
import controllers.journeys.routes._
import controllers.standard.routes._
import models._
import pages._
import pages.abroad.{SelfEmploymentAbroadCYAPage, SelfEmploymentAbroadPage}

class AbroadNavigatorSpec extends SpecBase {

  val navigator  = new AbroadNavigator
  val businessId = "SJPR05893938418"

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "must go from the Self-employment Abroad page to the Self Employment Abroad CYA page" in {

        navigator.nextPage(SelfEmploymentAbroadPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId)
      }

      "must go from the Abroad CYA page to the Section Completed page with Abroad journey" in {

        navigator.nextPage(SelfEmploymentAbroadCYAPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe
          SectionCompletedStateController.onPageLoad(taxYear, businessId, Abroad.toString, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"), taxYear, businessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from Self-employment Abroad page to the 'Check your details' page" in {

        navigator.nextPage(SelfEmploymentAbroadPage, CheckMode, UserAnswers("id"), taxYear, businessId) mustBe
          SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId)
      }

      "must go from a page that doesn't exist in the edit route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"), taxYear, businessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }
  }

}
