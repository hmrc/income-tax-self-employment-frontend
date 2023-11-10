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

import controllers.journeys.expenses.officeSupplies.routes.{OfficeSuppliesCYAController, OfficeSuppliesDisallowableAmountController}
import controllers.standard.routes.JourneyRecoveryController
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.expenses.WorkFromHomePage
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}

class OfficeSuppliesNavigatorSpec extends AnyWordSpec with Matchers {

  private val navigator = new OfficeSuppliesNavigator

  private val taxYear     = 2024
  private val userAnswers = UserAnswers("some_id")
  private val businessId  = "some_businessId"

  private val checkMode  = CheckMode
  private val normalMode = NormalMode

  "OfficeSuppliesNavigator" when {
    "navigating to the next page" when {
      "in CheckMode" must {
        "navigate to the JourneyRecoveryController" in {
          val expectedResult = JourneyRecoveryController.onPageLoad()

          navigator.nextPage(OfficeSuppliesAmountPage, checkMode, userAnswers, taxYear, businessId) shouldBe expectedResult
        }
      }
      "in NormalMode" when {
        "the page is OfficeSuppliesAmountPage" must {
          "navigate to the OfficeSuppliesDisallowableAmountController" in {
            val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, normalMode)

            navigator.nextPage(OfficeSuppliesAmountPage, normalMode, userAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OfficeSuppliesDisallowableAmountPage" must {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad()

            navigator.nextPage(OfficeSuppliesDisallowableAmountPage, normalMode, userAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "there is no match on the page, mode, userAnswers, taxYear, or businessId" must {
          "navigate to the JourneyRecoveryController" in {
            val someOtherJourneyPage = WorkFromHomePage

            val expectedResult = JourneyRecoveryController.onPageLoad()

            navigator.nextPage(someOtherJourneyPage, normalMode, userAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }
  }

}
