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
import models.journeys.capitalallowances.ZecAllowance
import models.journeys.capitalallowances.electricVehicleChargePoints._
import models.journeys.capitalallowances.zeroEmissionCars.ZecOnlyForSelfEmployment
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import pages.capitalallowances.electricVehicleChargePoints._
import pages.capitalallowances.tailoring.{ClaimCapitalAllowancesPage, SelectCapitalAllowancesPage}
import pages.capitalallowances.zeroEmissionCars._
import pages.capitalallowances.zeroEmissionGoodsVehicle._
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
          val data           = Json.obj("zecAllowance" -> ZecAllowance.Yes.toString)
          val expectedResult = zeroEmissionCars.routes.ZecTotalCostOfCarController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZecAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to ZeroEmissionCarsCYAController" in {
          val data           = Json.obj("zecAllowance" -> ZecAllowance.No.toString)
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
          val data           = Json.obj("zecOnlyForSelfEmployment" -> ZecOnlyForSelfEmployment.Yes.toString)
          val expectedResult = zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZegvOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to ZecUseOutsideSEPage" in {
          val data           = Json.obj("zecOnlyForSelfEmployment" -> ZecOnlyForSelfEmployment.No.toString)
          val expectedResult = zeroEmissionCars.routes.ZecUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZegvOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZegvOnlyForSelfEmploymentPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ZecUseOutsideSEPage" - {
      "navigate to ZecUsedForSelfEmploymentController" in {
        val expectedResult = zeroEmissionCars.routes.ZecHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(ZegvUseOutsideSEPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is ZecHowMuchDoYouWantToClaimPage" - {
      "navigate to ZecUsedForSelfEmploymentController" in {
        val expectedResult = zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)

        nextPage(ZegvHowMuchDoYouWantToClaimPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is ZeroEmissionGoodsVehiclePage" - {
      "answer is 'true'" - {
        "navigate to ZegvAllowancePage" in {
          val data           = Json.obj("zeroEmissionGoodsVehicle" -> true)
          val expectedResult = zeroEmissionGoodsVehicle.routes.ZegvAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ZeroEmissionGoodsVehiclePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'false'" - {
        "navigate to ZegvAllowancePage" in {
          val data           = Json.obj("zeroEmissionGoodsVehicle" -> false)
          val expectedResult = zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController.onPageLoad(taxYear, businessId)

          nextPage(ZeroEmissionGoodsVehiclePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ZeroEmissionGoodsVehiclePage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is EvcpAllowancePage" - {
      "answer is 'true'" - {
        "navigate to ChargePointTaxReliefController" in {
          val data           = Json.obj("evcpAllowance" -> true)
          val expectedResult = electricVehicleChargePoints.routes.ChargePointTaxReliefController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(EVCPAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'false'" - {
        "navigate to ElectricVehicleChargePointsCYAController" in {
          val data           = Json.obj("evcpAllowance" -> false)
          val expectedResult = electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

          nextPage(EVCPAllowancePage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(EVCPAllowancePage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is ChargePointTaxReliefPage" - {
      "answer is 'true'" - {
        "navigate to AmountSpentOnEvcpController" in {
          val data           = Json.obj("chargePointTaxRelief" -> true)
          val expectedResult = electricVehicleChargePoints.routes.AmountSpentOnEvcpController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(ChargePointTaxReliefPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'false'" - {
        "navigate to ElectricVehicleChargePointsCYAController" in {
          val data           = Json.obj("chargePointTaxRelief" -> false)
          val expectedResult = electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

          nextPage(ChargePointTaxReliefPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(ChargePointTaxReliefPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is AmountSpentOnEvcpPage" - {
      "navigate to EvcpOnlyForSelfEmploymentPage" in {
        val expectedResult = electricVehicleChargePoints.routes.EvcpOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(AmountSpentOnEvcpPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is EvcpOnlyForSelfEmploymentPage" - {
      "answer is 'Yes'" - {
        "navigate to EvcpHowMuchDoYouWantToClaimPage" in {
          val data           = Json.obj("evcpOnlyForSelfEmployment" -> EvcpOnlyForSelfEmployment.Yes.toString)
          val expectedResult = electricVehicleChargePoints.routes.EvcpHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(EvcpOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is 'No'" - {
        "navigate to EvcpUseOutsideSEPage" in {
          val data           = Json.obj("evcpOnlyForSelfEmployment" -> EvcpOnlyForSelfEmployment.No.toString)
          val expectedResult = electricVehicleChargePoints.routes.EvcpUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)

          nextPage(EvcpOnlyForSelfEmploymentPage, buildUserAnswers(data)) shouldBe expectedResult
        }
      }
      "answer is None or invalid" - {
        "navigate to the ErrorRecoveryPage" in {
          nextPage(EvcpOnlyForSelfEmploymentPage, emptyUserAnswers) shouldBe errorRedirect
        }
      }
    }

    "page is EvcpUseOutsideSEPage" - {
      "navigate to EvcpHowMuchDoYouWantToClaimPage" in {
        val expectedResult = electricVehicleChargePoints.routes.EvcpHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

        nextPage(EvcpUseOutsideSEPage, emptyUserAnswers) shouldBe expectedResult
      }
    }

    "page is EvcpHowMuchDoYouWantToClaimPage" - {
      "navigate to ElectricVehicleChargePointsCYAController" in {
        val expectedResult = electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController.onPageLoad(taxYear, businessId)

        nextPage(EvcpHowMuchDoYouWantToClaimPage, emptyUserAnswers) shouldBe expectedResult
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

    "page is ZecUsedForWorkPage, ZecAllowancePage, ZecTotalCostOfCarPage, ZecOnlyForSelfEmploymentPage, ZecUseOutsideSEPage or ZecHowMuchDoYouWantToClaimPage" - {
      "navigate to ZeroEmissionCarsCYAController" in {
        List(
          ZeroEmissionCarsPage,
          ZecAllowancePage,
          ZecTotalCostOfCarPage,
          ZegvOnlyForSelfEmploymentPage,
          ZegvUseOutsideSEPage,
          ZegvHowMuchDoYouWantToClaimPage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe zeroEmissionCars.routes.ZeroEmissionCarsCYAController.onPageLoad(taxYear, businessId)
        }
      }
    }

    "page is ZeroEmissionGoodsVehiclePage, ZegvTotalCostOfVehiclePage, ZegvAllowancePage" - {
      "navigate to ZeroEmissionCarsCYAController" in {
        List(ZeroEmissionGoodsVehiclePage, ZegvAllowancePage, ZegvTotalCostOfVehiclePage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe zeroEmissionGoodsVehicle.routes.ZeroEmissionGoodsVehicleCYAController
            .onPageLoad(taxYear, businessId)
        }
      }
    }

    "page is EVCPAllowancePage, ChargePointTaxReliefPage, AmountSpentOnEvcpPage, EvcpOnlyForSelfEmploymentPage, EvcpUseOutsideSEPage, EvcpHowMuchDoYouWantToClaimPage" - {
      "navigate to ElectricVehicleChargePointsCYAController" in {
        List(EVCPAllowancePage, ChargePointTaxReliefPage, AmountSpentOnEvcpPage, EvcpOnlyForSelfEmploymentPage, EvcpUseOutsideSEPage).foreach {
          nextPageViaCheckMode(_, emptyUserAnswers) shouldBe electricVehicleChargePoints.routes.ElectricVehicleChargePointsCYAController
            .onPageLoad(taxYear, businessId)
        }
      }
    }

    "navigate to journey recovery when there is no page match" in {
      nextPageViaCheckMode(UnknownPage, emptyUserAnswers) shouldBe errorRedirect
    }
  }
}
