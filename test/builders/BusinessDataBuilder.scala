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

import models.errors.HttpError
import models.domain.BusinessData
import models.domain.BusinessData.{AccountingPeriod, LatencyDetails}

object BusinessDataBuilder {

  val aBusinessData: Seq[BusinessData] = Seq(
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
    ),
    BusinessData(
      businessId = "businessId-2",
      typeOfBusiness = "self-employment",
      tradingName = Some("Trade two"),
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
  )

  val aBusinessDataNoneTradeNames: Seq[BusinessData] = Seq(
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
    ),
    BusinessData(
      businessId = "businessId-0-2",
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
  )

  val aBusinessDataCashAccounting: Seq[BusinessData] = Seq(
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
    ),
    BusinessData(
      businessId = "businessId-2",
      typeOfBusiness = "self-employment",
      tradingName = Some("Trade two"),
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
  )

  val aBusinessDataResponse: Either[HttpError, Seq[BusinessData]]     = Right(aBusinessData)
  val aBusinessDataNoneResponse: Either[HttpError, Seq[BusinessData]] = Right(aBusinessDataNoneTradeNames)

  lazy val aBusinessDataDataRequestStr: String =
    """
      |{
      |   "buisnessId":"SJPR05893938418",
      |   "typeOfBusiness":"self-employment",
      |   "tradingName":"string",
      |   "yearOfMigration":"2022",
      |   "accountingPeriods":[
      |      {
      |         "start":"2023-02-29",
      |         "end":"2024-02-29"
      |      }
      |   ],
      |   "firstAccountingPeriodStartDate":"2019-09-30",
      |   "firstAccountingPeriodEndDate":"2020-02-29",
      |   "latencyDetails":{
      |      "latencyEndDate":"2020-02-27",
      |      "taxYear1":"2019",
      |      "latencyIndicator1":"A",
      |      "taxYear2":"2020",
      |      "latencyIndicator2":"A"
      |   },
      |   "accountingType":"ACCRUAL",
      |   "commencementDate":"2023-04-06",
      |   "cessationDate":"2024-04-05",
      |   "businessAddressLineOne":"string",
      |   "businessAddressLineTwo  ":"string",
      |   "businessAddressLineThree":"string",
      |   "businessAddressLineFour ":"string",
      |   "businessAddressPostcode ":"string",
      |   "businessAddressCountryCode":"GB"
      |}
      |""".stripMargin

}
