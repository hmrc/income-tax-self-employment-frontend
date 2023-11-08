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
import controllers.journeys.tradeDetails.routes._
import controllers.standard.routes._
import models._
import models.database.UserAnswers
import models.journeys.TradeDetails
import pages._
import pages.tradeDetails.{CheckYourSelfEmploymentDetailsPage, SelfEmploymentSummaryPage}

class TradeDetailsNavigatorSpec extends SpecBase {

  val navigator  = new TradeDetailsNavigator

  case object UnknownPage extends Page

  "Navigator" - {

    "in Normal mode" - {

      "must go from the Check Your Self Employment Details page to the Self Employment Summary page" in {

        navigator.nextPage(CheckYourSelfEmploymentDetailsPage, NormalMode, UserAnswers("id"), taxYear, stubbedBusinessId) mustBe
          SelfEmploymentSummaryController.onPageLoad(taxYear)
      }

      "must go from the Self Employment Summary page to the Section Completed page with TradeDetails journey" in {

        navigator.nextPage(SelfEmploymentSummaryPage, NormalMode, UserAnswers("id"), taxYear, stubbedBusinessId) mustBe
          SectionCompletedStateController.onPageLoad(taxYear, stubbedBusinessId, TradeDetails.toString, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id"), taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to the Journey Recovery page" in {

        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"), taxYear, stubbedBusinessId) mustBe JourneyRecoveryController.onPageLoad()
      }
    }
  }

}
