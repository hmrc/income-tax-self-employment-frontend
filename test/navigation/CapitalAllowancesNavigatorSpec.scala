/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.journeys.capitalallowances.tailoring.routes._
import controllers.standard.routes._
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import play.api.libs.json.Json

class CapitalAllowancesNavigatorSpec extends SpecBase {

  val navigator = new CapitalAllowancesNavigator

  def nextPage(currentPage: Page, answers: UserAnswers) =
    navigator.nextPage(currentPage, NormalMode, answers, taxYear, businessId)

  def nextPageViaCheckMode(currentPage: Page, answers: UserAnswers) =
    navigator.nextPage(currentPage, CheckMode, answers, taxYear, businessId)

  case object UnknownPage extends Page

  "NormalMode" - {
    "page is ClaimCapitalAllowancesPage" - {
      "answer is true" - {
        "navigate to SelectCapitalAllowancesController" in {
          val data           = Json.obj("claimCapitalAllowances" -> true)
          val expectedResult = SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ClaimCapitalAllowancesPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is false" - {
        "navigate to CapitalAllowanceCYAController" in {
          val data           = Json.obj("claimCapitalAllowances" -> false)
          val expectedResult = CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

          nextPage(ClaimCapitalAllowancesPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
    }
    "navigate to journey recovery on no page match" in {
      nextPage(UnknownPage, emptyUserAnswers) shouldBe JourneyRecoveryController.onPageLoad()
    }
  }
  "CheckMode" - {
    "page is ClaimCapitalAllowancesPage or SelectCapitalAllowancesPage" - {
      "navigate to CapitalAllowanceCYAController" in {
        List(ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)
        }
      }
    }
  }
}
