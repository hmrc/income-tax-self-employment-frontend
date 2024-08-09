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

package builders

import models.domain.BusinessData
import models.domain.BusinessData.{AccountingPeriod, LatencyDetails}
import models.journeys.nics.TaxableProfitAndLoss

object BusinessDataBuilder {

  val aBusinessData: BusinessData =
    BusinessData(
      businessId = "businessId-1",
      typeOfBusiness = "self-employment",
      tradingName = Some("Trade one"),
      yearOfMigration = Some("2022"),
      accountingPeriods = Seq(AccountingPeriod("2023-0x2-29", "2024-0x2-29")),
      firstAccountingPeriodStartDate = Some("2019-09-30"),
      firstAccountingPeriodEndDate = Some("2020-02-29"),
      latencyDetails = Some(LatencyDetails("2020-02-27", "2019", "A", "2020", "A")),
      accountingType = Some("ACCRUAL"),
      commencementDate = Some("2023-04-06"),
      cessationDate = Some("2024-04-05"),
      businessAddressLineOne = "Business Address",
      businessAddressLineTwo = Some("Business Address 2"),
      businessAddressLineThree = Some("Business Address 3"),
      businessAddressLineFour = Some("Business Address 4"),
      businessAddressPostcode = Some("Business Address 5"),
      businessAddressCountryCode = "GB"
    )

  val aBusinessDataNoneTradeNames: BusinessData =
    BusinessData(
      businessId = "businessId-0-1",
      typeOfBusiness = "self-employment",
      tradingName = None,
      yearOfMigration = Some("2022"),
      accountingPeriods = Seq(AccountingPeriod("2023-0x2-29", "2024-0x2-29")),
      firstAccountingPeriodStartDate = Some("2019-09-30"),
      firstAccountingPeriodEndDate = Some("2020-02-29"),
      latencyDetails = Some(LatencyDetails("2020-02-27", "2019", "A", "2020", "A")),
      accountingType = Some("ACCRUAL"),
      commencementDate = Some("2023-04-06"),
      cessationDate = Some("2024-04-05"),
      businessAddressLineOne = "Business Address",
      businessAddressLineTwo = Some("Business Address 2"),
      businessAddressLineThree = Some("Business Address 3"),
      businessAddressLineFour = Some("Business Address 4"),
      businessAddressPostcode = Some("Business Address 5"),
      businessAddressCountryCode = "GB"
    )

  val aBusinessDataCashAccounting: BusinessData =
    BusinessData(
      businessId = "businessId-1",
      typeOfBusiness = "self-employment",
      tradingName = Some("Trade one"),
      yearOfMigration = Some("2022"),
      accountingPeriods = Seq(AccountingPeriod("2023-0x2-29", "2024-0x2-29")),
      firstAccountingPeriodStartDate = Some("2019-09-30"),
      firstAccountingPeriodEndDate = Some("2020-02-29"),
      latencyDetails = Some(LatencyDetails("2020-02-27", "2019", "A", "2020", "A")),
      accountingType = Some("CASH"),
      commencementDate = Some("2023-04-06"),
      cessationDate = Some("2024-04-05"),
      businessAddressLineOne = "Business Address",
      businessAddressLineTwo = Some("Business Address 2"),
      businessAddressLineThree = Some("Business Address 3"),
      businessAddressLineFour = Some("Business Address 4"),
      businessAddressPostcode = Some("Business Address 5"),
      businessAddressCountryCode = "GB"
    )

  val withLossesTaxableProfitAndLoss   = List(TaxableProfitAndLoss(100, 0), TaxableProfitAndLoss(100, 50))
  val smallProfitTaxableProfitAndLoss  = List(TaxableProfitAndLoss(100, 0), TaxableProfitAndLoss(100, 0))
  val mediumProfitTaxableProfitAndLoss = List(TaxableProfitAndLoss(6800, 0))
  val largeProfitTaxableProfitAndLoss  = List(TaxableProfitAndLoss(14000, 0))
}
