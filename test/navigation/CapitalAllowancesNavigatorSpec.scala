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
import controllers.journeys.capitalallowances.{tailoring, zeroEmissionCars}
import controllers.standard
import models.database.UserAnswers
import models.journeys.capitalallowances.ZeroEmissionCarsAllowance
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.zeroEmissionCars.{ZecAllowancePage, ZecTotalCostOfCarPage, ZecUsedForWorkPage}
import play.api.libs.json.Json
import play.api.mvc.Call

class CapitalAllowancesNavigatorSpec extends SpecBase {

  val navigator = new CapitalAllowancesNavigator

  def nextPage(currentPage: Page, answers: UserAnswers): Call =
    navigator.nextPage(currentPage, NormalMode, answers, taxYear, businessId)

  def nextPageViaCheckMode(currentPage: Page, answers: UserAnswers): Call =
    navigator.nextPage(currentPage, CheckMode, answers, taxYear, businessId)

  case object UnknownPage extends Page

  private val errorRedirect = standard.routes.JourneyRecoveryController.onPageLoad()

  "NormalMode" - {

    "page is ClaimCapitalAllowancesPage" - {
      "answer is true" - {
        "navigate to SelectCapitalAllowancesController" in {
          val data           = Json.obj("claimCapitalAllowances" -> true)
          val expectedResult = tailoring.routes.SelectCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ClaimCapitalAllowancesPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is false" - {
        "navigate to CapitalAllowanceCYAController" in {
          val data           = Json.obj("claimCapitalAllowances" -> false)
          val expectedResult = tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)

          nextPage(ClaimCapitalAllowancesPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ClaimCapitalAllowancesPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecUsedForWorkPage" - {
      "answer is true" - {
        "navigate to ZecAllowanceController" in {
          val data           = Json.obj("zeroEmissionCarsUsedForWork" -> true)
          val expectedResult = zeroEmissionCars.routes.ZecAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecUsedForWorkPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is false" - {
        "navigate to ZeroEmissionCarsCYAController" in {
          val data           = Json.obj("zeroEmissionCarsUsedForWork" -> false)
          val expectedResult = zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

          nextPage(ZecUsedForWorkPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZecUsedForWorkPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecAllowancePage" - {
      "answer is 'Yes'" - {
        "navigate to TotalCostOfCarController" in {
          val data           = Json.obj("zeroEmissionCarsAllowance" -> ZeroEmissionCarsAllowance.Yes.toString)
          val expectedResult = zeroEmissionCars.routes.ZecTotalCostOfCarController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to ZeroEmissionCarsCYAController" in {
          val data           = Json.obj("zeroEmissionCarsAllowance" -> ZeroEmissionCarsAllowance.No.toString)
          val expectedResult = zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

          nextPage(ZecAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZecAllowancePage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecTotalCostOfCarPage" - {
      "navigate to ZecUsedForSelfEmploymentController" in {
        val expectedResult = zeroEmissionCars.routes.ZecUsedForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(ZecTotalCostOfCarPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "navigate to journey recovery on no page match" in {
      nextPage(UnknownPage, emptyUserAnswers) shouldBe errorRedirect
    }
  }
  "CheckMode" - {
    "page is ClaimCapitalAllowancesPage or SelectCapitalAllowancesPage" - {
      "navigate to CapitalAllowanceCYAController" in {
        List(ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe tailoring.routes.CapitalAllowanceCYAController.onPageLoad(taxYear, businessId)
        }
      }
    }
  }
}
