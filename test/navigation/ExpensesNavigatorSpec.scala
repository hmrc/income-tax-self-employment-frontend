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
import controllers.journeys.expenses._
import controllers.journeys.expenses.officeSupplies.routes._
import controllers.journeys.expenses.otherExpenses.routes._
import controllers.standard.routes._
import models._
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.DisallowableStaffCostsPage
import play.api.libs.json.Json

class ExpensesNavigatorSpec extends SpecBase {

  val navigator = new ExpensesNavigator

  case object UnknownPage extends Page

  "ExpensesNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        "OfficeSupplies journey" - {
          "the page is OfficeSuppliesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the OfficeSuppliesDisallowableAmountController" in {
                val data        = Json.obj(businessId.value -> Json.obj("officeSupplies" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(OfficeSuppliesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the OfficeSuppliesCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("officeSupplies" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is OfficeSuppliesDisallowableAmountPage" - {
            "navigate to the OfficeSuppliesCYAController" in {
              val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }
        "OtherExpenses journey" - {
          "the page is OtherExpensesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the OtherExpensesDisallowableAmountController" in {
                val data        = Json.obj(businessId.value -> Json.obj("otherExpenses" -> "yesDisallowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(OtherExpensesAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
            "all expenses were claimed as allowable" - {
              "navigate to the OtherExpensesCYAController" in {
                val data        = Json.obj(businessId.value -> Json.obj("otherExpenses" -> "yesAllowable"))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, userAnswers, taxYear, businessId) shouldBe expectedResult
              }
            }
          }
          "the page is OtherExpensesDisallowableAmountPage" - {
            "navigate to the OtherExpensesCYAController" in {
              val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "StaffCosts journey" - {
          "the page is StaffCostsAmountPage" - {
            "should navigate to the StaffCostsDisallowableAmountController" - {
              "when expenses are disallowable" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, true, Some(businessId)).success.value

                val expectedResult =
                  staffCosts.routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "should navigate to the StaffCostsCYAController" - {
              "when expenses are not disallowable" in {
                val userAnswers = emptyUserAnswers.set(DisallowableStaffCostsPage, false, Some(businessId)).success.value

                val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(StaffCostsAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is StaffCostsDisallowableAmountController" - {
            "navigate to the StaffCostsCYAController" in {
              val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(StaffCostsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "DisallowableProfessionalFees journey" - {
          "the page is ProfessionalFeesAmountPage" - {
            "some expenses were claimed to be disallowable" - {
              "navigate to the ProfessionalFeesDisallowableAmountPage" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableProfessionalFees" -> true))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = professionalFees.routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, mode)

                navigator.nextPage(ProfessionalFeesAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
            "navigate to the ProfessionalFeesCYAPage" - {
              "if all expenses were claimed as allowable" in {
                val data        = Json.obj(businessId.value -> Json.obj("disallowableProfessionalFees" -> false))
                val userAnswers = UserAnswers(userAnswersId, data)

                val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

                navigator.nextPage(ProfessionalFeesAmountPage, mode, userAnswers, taxYear, businessId) mustBe expectedResult
              }
            }
          }
          "the page is ProfessionalFeesDisallowableAmountController" - {
            "navigate to the ProfessionalFeesCYAController" in {
              val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

              navigator.nextPage(ProfessionalFeesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
            }
          }
        }

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }

      "in CheckMode" - {
        val mode = CheckMode

        "the page is OfficeSuppliesAmountPage" - {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OfficeSuppliesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OfficeSuppliesDisallowableAmountPage" - {
          "navigate to the OfficeSuppliesCYAController" in {
            val expectedResult = OfficeSuppliesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is OtherExpensesAmountPage" - {
          "navigate to the OtherExpensesCYAController" in {
            val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OtherExpensesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is OtherExpensesDisallowableAmountPage" - {
          "navigate to the OtherExpensesCYAController" in {
            val expectedResult = OtherExpensesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(OtherExpensesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is StaffCostsAmountPage" - {
          "navigate to the StaffCostsCYAController" in {
            val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(StaffCostsAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is StaffCostsDisallowableAmountPage" - {
          "navigate to the StaffCostsCYAController" in {
            val expectedResult = staffCosts.routes.StaffCostsCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(StaffCostsDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "the page is ProfessionalFeesAmountPage" - {
          "navigate to the ProfessionalFeesCYAController" in {
            val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ProfessionalFeesAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
        "the page is ProfessionalFeesDisallowableAmountPage" - {
          "navigate to the ProfessionalFeesCYAController" in {
            val expectedResult = professionalFees.routes.ProfessionalFeesCYAController.onPageLoad(taxYear, businessId)

            navigator.nextPage(ProfessionalFeesDisallowableAmountPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }
  }

}
