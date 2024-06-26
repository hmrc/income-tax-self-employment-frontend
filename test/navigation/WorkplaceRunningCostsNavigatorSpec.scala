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
import controllers.journeys.expenses.workplaceRunningCosts
import controllers.standard
import models._
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.WorkFromBusinessPremises
import models.journeys.expenses.workplaceRunningCosts.{WfbpFlatRateOrActualCosts, WfhFlatRateOrActualCosts}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.tailoring.individualCategories.WorkFromBusinessPremisesPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import pages.expenses.workplaceRunningCosts.workingFromHome._

class WorkplaceRunningCostsNavigatorSpec extends SpecBase {

  val navigator = new WorkplaceRunningCostsNavigator

  case object UnknownPage extends Page

  private def userAnswersBPAllowable(baseAnswers: UserAnswers) =
    baseAnswers.set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.YesAllowable, Some(businessId)).success.value
  private def userAnswersBPDisallowable(baseAnswers: UserAnswers) =
    baseAnswers.set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.YesDisallowable, Some(businessId)).success.value
  private def userAnswersBPNo(baseAnswers: UserAnswers) =
    baseAnswers.set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.No, Some(businessId)).success.value

  private def liveAtBPResult(mode: Mode) =
    workplaceRunningCosts.workingFromBusinessPremises.routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, mode)
  private def cyaResult =
    workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController.onPageLoad(taxYear, businessId)
  private def errorResult = standard.routes.JourneyRecoveryController.onPageLoad()

  "WorkplaceRunningCostsNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        "the page is MoreThan25HoursPage" - {
          "the user answers 'Yes'" - {
            "navigate to the WorkingFromHomeHoursController" in {
              val userAnswers    = emptyUserAnswers.set(MoreThan25HoursPage, true, Some(businessId)).success.value
              val expectedResult = workplaceRunningCosts.workingFromHome.routes.WorkingFromHomeHoursController.onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(MoreThan25HoursPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
            }
          }
          "the user answers 'No'" - {
            "navigate to the WfhExpensesInfoController" in {
              val userAnswers    = emptyUserAnswers.set(MoreThan25HoursPage, false, Some(businessId)).success.value
              val expectedResult = workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(MoreThan25HoursPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
            }
          }
        }

        "the page is WorkingFromHomeHoursPage" - {
          "navigate to the WfhFlatRateOrActualCostsController" in {
            val expectedResult =
              workplaceRunningCosts.workingFromHome.routes.WfhFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, mode)

            navigator.nextPage(WorkingFromHomeHoursPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is WfhFlatRateOrActualCostsPage" - {
          "the answer is Actual Costs" - {
            "navigate to the WfhExpensesInfoController" in {
              val userAnswers =
                emptyUserAnswers.set(WfhFlatRateOrActualCostsPage, WfhFlatRateOrActualCosts.ActualCosts, Some(businessId)).success.value
              val expectedResult =
                workplaceRunningCosts.workingFromHome.routes.WfhExpensesInfoController.onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the answer is Flat Rate, and WorkingFromBusinessPremises is Allowable or Disallowable" - {
            "navigate to the DoYouLiveAtYourBusinessPremisesController" in {
              val userAnswers =
                emptyUserAnswers.set(WfhFlatRateOrActualCostsPage, WfhFlatRateOrActualCosts.FlatRate, Some(businessId)).success.value

              navigator.nextPage(
                WfhFlatRateOrActualCostsPage,
                mode,
                userAnswersBPDisallowable(userAnswers),
                taxYear,
                businessId) shouldBe liveAtBPResult(mode)
              navigator.nextPage(
                WfhFlatRateOrActualCostsPage,
                mode,
                userAnswersBPAllowable(userAnswers),
                taxYear,
                businessId) shouldBe liveAtBPResult(mode)
              navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, userAnswersBPNo(userAnswers), taxYear, businessId) shouldBe cyaResult
              navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe errorResult
            }
          }
        }

        "the page is WfhExpensesInfoPage" - {
          "navigate to the WfhClaimingAmountController" in {
            val expectedResult =
              workplaceRunningCosts.workingFromHome.routes.WfhClaimingAmountController.onPageLoad(taxYear, businessId, mode)

            navigator.nextPage(WfhExpensesInfoPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is WfhClaimingAmountPage" - {
          "the BusinessPremises tailoring answer is 'YesAllowable' or 'YesDisallowable" - {
            "navigate to the LiveAtBusinessPremisesController" in {

              navigator.nextPage(
                WfhClaimingAmountPage,
                mode,
                userAnswersBPDisallowable(emptyUserAnswers),
                taxYear,
                businessId) shouldBe liveAtBPResult(mode)
              navigator.nextPage(WfhClaimingAmountPage, mode, userAnswersBPAllowable(emptyUserAnswers), taxYear, businessId) shouldBe liveAtBPResult(
                mode)
            }
          }
          "the BusinessPremises tailoring answer is 'No'" - {
            "navigate to the WfhExpensesInfoController" in {

              navigator.nextPage(WfhClaimingAmountPage, mode, userAnswersBPNo(emptyUserAnswers), taxYear, businessId) shouldBe cyaResult
            }
          }
          "there isn't relevant tailoring data" - {
            "navigate to the JourneyRecoveryController" in {

              navigator.nextPage(WfhClaimingAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe errorResult
            }
          }
        }

        "the page is LiveAtBusinessPremisesPage" - {
          "navigate to the BusinessPremisesAmountController" in {
            val expectedResult =
              workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, mode)

            navigator.nextPage(LiveAtBusinessPremisesPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is BusinessPremisesAmountPage" - {
          "the BusinessPremises tailoring answer is 'YesDisallowable" - {
            "navigate to the BusinessPremisesDisallowableAmountController" in {
              val expectedResult =
                workplaceRunningCosts.workingFromBusinessPremises.routes.BusinessPremisesDisallowableAmountController
                  .onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(
                BusinessPremisesAmountPage,
                mode,
                userAnswersBPDisallowable(emptyUserAnswers),
                taxYear,
                businessId) shouldBe expectedResult
            }
          }
          "the BusinessPremises tailoring answer is 'YesAllowable'" - {
            "and the user has answered 'Yes' to living at Business Premises" - {
              "navigate to the PeopleLivingAtBusinessPremisesController" in {
                val userAnswers = emptyUserAnswers.set(LiveAtBusinessPremisesPage, true, Some(businessId)).success.value
                val expectedResult =
                  workplaceRunningCosts.workingFromBusinessPremises.routes.PeopleLivingAtBusinessPremisesController
                    .onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(BusinessPremisesAmountPage, mode, userAnswersBPAllowable(userAnswers), taxYear, businessId) shouldBe expectedResult
              }
            }
            "and the user has answered 'No' to living at Business Premises" - {
              "navigate to the CyaController" in {
                val userAnswers = emptyUserAnswers.set(LiveAtBusinessPremisesPage, false, Some(businessId)).success.value

                navigator.nextPage(BusinessPremisesAmountPage, mode, userAnswersBPAllowable(userAnswers), taxYear, businessId) shouldBe cyaResult
              }
            }
          }
          "there isn't relevant data" - {
            "navigate to the JourneyRecoveryController" in {

              navigator.nextPage(WfhFlatRateOrActualCostsPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe errorResult
            }
          }
        }

        "the page is BusinessPremisesDisallowableAmountPage" - {
          "the LiveAtBusinessPremises answer is 'Yes" - {
            "navigate to the peopleLivingAtBusinessPremisesPage" in {
              val userAnswers = emptyUserAnswers.set(LiveAtBusinessPremisesPage, true, Some(businessId)).success.value
              val expectedResult =
                workplaceRunningCosts.workingFromBusinessPremises.routes.PeopleLivingAtBusinessPremisesController
                  .onPageLoad(taxYear, businessId, mode)

              navigator.nextPage(BusinessPremisesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the LiveAtBusinessPremises answer is 'No" - {
            "navigate to the CYA page" in {
              val userAnswers = emptyUserAnswers.set(LiveAtBusinessPremisesPage, false, Some(businessId)).success.value
              val expectedResult =
                workplaceRunningCosts.routes.WorkplaceRunningCostsCYAController
                  .onPageLoad(taxYear, businessId)

              navigator.nextPage(BusinessPremisesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "there isn't relevant data" - {
            "navigate to the JourneyRecoveryController" in {

              navigator.nextPage(BusinessPremisesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe errorResult
            }
          }
        }

        "the page is PeopleLivingAtBusinessPremisesPage" - {
          "navigate to the WfbpFlatRateOrActualCostsController" in {
            val expectedResult =
              workplaceRunningCosts.workingFromBusinessPremises.routes.WfbpFlatRateOrActualCostsController.onPageLoad(taxYear, businessId, mode)

            navigator.nextPage(PeopleLivingAtBusinessPremisesPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is WfbpFlatRateOrActualCostsPage" - {
          "the answer is Actual Costs" - {
            "navigate to the WfbpClaimAmountController" in {
              val userAnswers =
                emptyUserAnswers.set(WfbpFlatRateOrActualCostsPage, WfbpFlatRateOrActualCosts.ActualCosts, Some(businessId)).success.value
              val expectedResult =
                workplaceRunningCosts.workingFromBusinessPremises.routes.WfbpClaimingAmountController.onPageLoad(taxYear, businessId, NormalMode)

              navigator.nextPage(WfbpFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
          "the answer is Flat Rate" - {
            "navigate to the CYA page" in {
              val userAnswers =
                emptyUserAnswers.set(WfbpFlatRateOrActualCostsPage, WfbpFlatRateOrActualCosts.FlatRate, Some(businessId)).success.value

              navigator.nextPage(WfbpFlatRateOrActualCostsPage, mode, userAnswers, taxYear, businessId) shouldBe cyaResult
            }
          }
        }

        "the page is WfbpClaimingAmountPage" - {
          "navigate to the CYA page" in {

            navigator.nextPage(WfbpClaimingAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe cyaResult
          }
        }

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }

      "in CheckMode" - {
        val mode = CheckMode

        // TODO 6997 add check mode navigation tests for CYA page

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }
  }

}
