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

package models.journeys

import enumeratum._
import models.common.PageName
import pages.expenses.advertisingOrMarketing.AdvertisingOrMarketingAmountPage
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import pages.expenses.depreciation.DepreciationDisallowableAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.interest.{InterestAmountPage, InterestDisallowableAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.income._
import play.api.mvc.PathBindable

sealed abstract class Journey(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  val pageKeys: List[PageName] = Nil
}

object Journey extends Enum[Journey] with utils.PlayJsonEnum[Journey] {
  val values: IndexedSeq[Journey] = findValues

  case object TradeDetails extends Journey("trade-details")
  case object Abroad       extends Journey("self-employment-abroad")
  case object Income extends Journey("income") {
    override val pageKeys: List[PageName] = List(
      AnyOtherIncomePage.pageName,
      HowMuchTradingAllowancePage.pageName,
      IncomeCYAPage.pageName,
      IncomeNotCountedAsTurnoverPage.pageName,
      NonTurnoverIncomeAmountPage.pageName,
      OtherIncomeAmountPage.pageName,
      TradingAllowanceAmountPage.pageName,
      TradingAllowancePage.pageName,
      TurnoverIncomeAmountPage.pageName,
      TurnoverNotTaxablePage.pageName
    )
  }
  case object ExpensesTotal extends Journey("expenses-total")
  case object ExpensesTailoring extends Journey("expenses-categories") {
    override val pageKeys: List[PageName] = List(
      ExpensesCategoriesPage.pageName
    )
  }
  case object ExpensesGoodsToSellOrUse extends Journey("expenses-goods-to-sell-or-use") {
    override val pageKeys: List[PageName] = List(GoodsToSellOrUseAmountPage.pageName, DisallowableGoodsToSellOrUseAmountPage.pageName)
  }
  case object ExpensesAdvertisingOrMarketing extends Journey("expenses-advertising-marketing") {
    override val pageKeys: List[PageName] = List(AdvertisingOrMarketingAmountPage.pageName)
  }
  case object ExpensesOfficeSupplies extends Journey("expenses-office-supplies") {
    override val pageKeys: List[PageName] = List(OfficeSuppliesAmountPage.pageName, OfficeSuppliesDisallowableAmountPage.pageName)
  }
  case object ExpensesOtherExpenses extends Journey("expenses-other-expenses") {
    override val pageKeys: List[PageName] = List(OtherExpensesAmountPage.pageName, OtherExpensesDisallowableAmountPage.pageName)
  }
  case object ExpensesEntertainment extends Journey("expenses-entertainment") {
    override val pageKeys: List[PageName] = List(EntertainmentAmountPage.pageName)
  }
  case object ExpensesStaffCosts extends Journey("expenses-staff-costs") {
    override val pageKeys: List[PageName] = List(StaffCostsAmountPage.pageName, StaffCostsDisallowableAmountPage.pageName)
  }
  case object ExpensesConstruction extends Journey("expenses-construction") {
    override val pageKeys: List[PageName] = List(ConstructionIndustryAmountPage.pageName, ConstructionIndustryDisallowableAmountPage.pageName)
  }
  case object ExpensesProfessionalFees extends Journey("expenses-professional-fees") {
    override val pageKeys: List[PageName] = List(ProfessionalFeesAmountPage.pageName, ProfessionalFeesDisallowableAmountPage.pageName)
  }
  case object ExpensesInterest extends Journey("expenses-interest") {
    override val pageKeys: List[PageName] = List(InterestAmountPage.pageName, InterestDisallowableAmountPage.pageName)
  }
  case object ExpensesDepreciation extends Journey("expenses-depreciation") {
    override val pageKeys: List[PageName] = List(DepreciationDisallowableAmountPage.pageName)
  }

  case object ExpensesRepairsAndMaintenance extends Journey("expenses-repairs-and-maintenance") {
    override val pageKeys: List[PageName] = List(RepairsAndMaintenanceAmountPage.pageName, RepairsAndMaintenanceDisallowableAmountPage.pageName)
  }

  case object NationalInsurance extends Journey("national-insurance")

  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[Journey] = new PathBindable[Journey] {

    override def bind(key: String, value: String): Either[String, Journey] =
      strBinder.bind(key, value).flatMap { stringValue =>
        Journey.withNameOption(stringValue) match {
          case Some(journeyName) => Right(journeyName)
          case None              => Left(s"$stringValue Invalid journey name")
        }
      }

    override def unbind(key: String, journeyName: Journey): String =
      strBinder.unbind(key, journeyName.entryName)
  }
}
