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

import models.common.PageName
import pages.expenses.construction.ConstructionIndustryAmountPage
import pages.expenses.entertainment.EntertainmentAmountPage
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.income._
import play.api.libs.json._

sealed trait Journey {
  val pageKeys: List[PageName] = Nil
}

object Journey {

  case object TradeDetails extends Journey {
    override def toString: String = "trade-details"
  }

  case object Abroad extends Journey {
    override def toString: String = "self-employment-abroad"
  }

  case object Income extends Journey {
    override def toString: String = "income"

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

  case object ExpensesTotal extends Journey {
    override def toString: String = "expenses-total"
  }

  case object ExpensesTailoring extends Journey {
    override def toString: String = "expenses-categories"
  }

  case object ExpensesGoodsToSellOrUse extends Journey {
    override def toString: String = "expenses-goods-to-sell-or-use"

    override val pageKeys: List[PageName] = List(GoodsToSellOrUseAmountPage.pageName, DisallowableGoodsToSellOrUseAmountPage.pageName)
  }

  case object ExpensesAdvertisingOrMarketing extends Journey {
    override def toString: String = "expenses-advertising-marketing"
  }

  case object ExpensesOfficeSupplies extends Journey {
    override def toString: String = "expenses-office-supplies"

    override val pageKeys: List[PageName] = List(OfficeSuppliesAmountPage.pageName, OfficeSuppliesDisallowableAmountPage.pageName)
  }

  case object ExpensesEntertainment extends Journey {
    override def toString: String = "expenses-entertainment"

    override val pageKeys: List[PageName] = List(EntertainmentAmountPage.pageName)
  }

  case object ExpensesStaffCosts extends Journey {
    override def toString: String = "expenses-staff-costs"
  }

  case object ExpensesConstruction extends Journey {
    override def toString: String = "expenses-construction"

    override val pageKeys: List[PageName] = List(ConstructionIndustryAmountPage.pageName)
  }

  case object NationalInsurance extends Journey {
    override def toString: String = "national-insurance"
  }

  case object ExpensesRepairsAndMaintenance extends Journey {
    override def toString: String = "expenses-repairs-and-maintenance"

    override val pageKeys: List[PageName] = List(RepairsAndMaintenanceAmountPage.pageName, RepairsAndMaintenanceDisallowableAmountPage.pageName)
  }

  val journeyReads: Reads[Journey] = Reads[Journey] {
    case JsString("trade-details")                    => JsSuccess(TradeDetails)
    case JsString("self-employment-abroad")           => JsSuccess(Abroad)
    case JsString("income")                           => JsSuccess(Income)
    case JsString("expenses-total")                   => JsSuccess(ExpensesTotal)
    case JsString("expenses-categories")              => JsSuccess(ExpensesTailoring)
    case JsString("expenses-goods-to-sell-or-use")    => JsSuccess(ExpensesGoodsToSellOrUse)
    case JsString("expenses-advertising-marketing")   => JsSuccess(ExpensesAdvertisingOrMarketing)
    case JsString("expenses-entertainment")           => JsSuccess(ExpensesEntertainment)
    case JsString("expenses-construction")            => JsSuccess(ExpensesConstruction)
    case JsString("expenses-office-supplies")         => JsSuccess(ExpensesOfficeSupplies)
    case JsString("expenses-repairs-and-maintenance") => JsSuccess(ExpensesRepairsAndMaintenance)
    case JsString("expenses-staff-costs")             => JsSuccess(ExpensesStaffCosts)
    case JsString("national-insurance")               => JsSuccess(NationalInsurance)
    case _                                            => JsError("Parsing error")
  }

  val journeyWrites: Writes[Journey] = Writes[Journey] { case journey @ _ =>
    JsString(journey.toString)
  }

  implicit val journeyFormat: Format[Journey] = Format(journeyReads, journeyWrites)
}
