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
import controllers.journeys.capitalallowances._
import controllers.standard
import models.database.UserAnswers
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import pages.capitalallowances.balancingCharge.{BalancingChargeAmountPage, BalancingChargePage}
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.zeroEmissionCars._
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

    "page is ZeroEmissionCarsPage" - {
      "answer is true" - {
        "navigate to ZecAllowanceController" in {
          val data           = Json.obj("zeroEmissionCars" -> true)
          val expectedResult = zeroEmissionCars.routes.ZecAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZeroEmissionCarsPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is false" - {
        "navigate to ZeroEmissionCarsCYAController" in {
          val data           = Json.obj("zeroEmissionCars" -> false)
          val expectedResult = zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

          nextPage(ZeroEmissionCarsPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZeroEmissionCarsPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecAllowancePage" - {
      "answer is 'Yes'" - {
        "navigate to TotalCostOfCarController" in {
          val data           = Json.obj("zecAllowance" -> true)
          val expectedResult = zeroEmissionCars.routes.ZecTotalCostOfCarController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to ZeroEmissionCarsCYAController" in {
          val data           = Json.obj("zecAllowance" -> false)
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
        val expectedResult = zeroEmissionCars.routes.ZecOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(ZecTotalCostOfCarPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is ZecOnlyForSelfEmploymentPage" - {
      "answer is 'Yes'" - {
        "navigate to ZecHowMuchDoYouWantToClaimPage" in {
          val data           = Json.obj("zecOnlyForSelfEmployment" -> true)
          val expectedResult = zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to ZecUseOutsideSEPage" in {
          val data           = Json.obj("zecOnlyForSelfEmployment" -> false)
          val expectedResult = zeroEmissionCars.routes.ZecUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZecOnlyForSelfEmploymentPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecUseOutsideSEPage" - {
      "navigate to ZecUsedForSelfEmploymentController" in {
        val expectedResult = zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(ZecUseOutsideSEPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is ZecHowMuchDoYouWantToClaimPage" - {
      "navigate to ZecUsedForSelfEmploymentController" in {
        val expectedResult = zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

        nextPage(ZecHowMuchDoYouWantToClaimPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "navigate to journey recovery on no page match" in {
      nextPage(UnknownPage, emptyUserAnswers) shouldBe errorRedirect
    }

  }

  "page is BalancingCharge" - {
    "answer is true" - {
      "navigate to BalancingChargeAmountController" in {
        val data           = Json.obj("balancingCharge" -> true)
        val expectedResult = balancingCharge.routes.BalancingChargeAmountController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(BalancingChargePage, buildUserAnswers(data)) shouldBe expectedResult
      }
    }
    "answer is false" - {
      "navigate to BalancingChargeCYAController" in {
        val data           = Json.obj("balancingCharge" -> false)
        val expectedResult = balancingCharge.routes.BalancingChargeCYAController.onPageLoad(taxYear, businessId)

        nextPage(BalancingChargePage, buildUserAnswers(data)) shouldBe expectedResult
      }
    }

    "navigate to BalancingChargeCYAPage from BalancingChargeAmountPage" in {
      val data           = Json.obj("balancingCharge" -> true, "balancingChargeAmount" -> 123.00)
      val expectedResult = balancingCharge.routes.BalancingChargeCYAController.onPageLoad(taxYear, businessId)

      nextPage(BalancingChargeAmountPage, buildUserAnswers(data)) shouldBe expectedResult
    }

    "answer is None or invalid" - {
      "navigate to the ErrorRecoveryPage" in {
        nextPage(BalancingChargePage, emptyUserAnswers) shouldBe errorRedirect
      }
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

    "page is ZecUsedForWorkPage, ZecAllowancePage, ZecTotalCostOfCarPage, ZecOnlyForSelfEmploymentPage, ZecUseOutsideSEPage or ZecHowMuchDoYouWantToClaimPage" - {
      "navigate to ZeroEmissionCarsCYAController" in {
        List(
          ZeroEmissionCarsPage,
          ZecAllowancePage,
          ZecTotalCostOfCarPage,
          ZecOnlyForSelfEmploymentPage,
          ZecUseOutsideSEPage,
          ZecHowMuchDoYouWantToClaimPage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
        }
      }
    }

    "navigate to journey recovery when there is no page match" in {
      nextPageViaCheckMode(UnknownPage, emptyUserAnswers) shouldBe errorRedirect
    }
  }

  "page is BalancingChargePage or BalancingChargeAmountPage" - {
    "navigate to BalancingAllowanceAmountController" in {
      List(BalancingChargePage, BalancingChargeAmountPage).foreach {
        nextPageViaCheckMode(_, emptyUserAnswers) shouldBe balancingCharge.routes.BalancingChargeCYAController.onPageLoad(taxYear, businessId)
      }
    }
  }
}
