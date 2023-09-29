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

import models.requests.BusinessData

object BusinessDataBuilder {

  val aBusinessData1 = BusinessData(
    businessId = "id1",
    typeOfBusiness = "Name 1",
    tradingName = None,
    yearOfMigration = None,
    accountingPeriods = Seq.empty,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = None,
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = "example",
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = "example"
  )

  val aBusinessData2 = BusinessData(
    businessId = "id2",
    typeOfBusiness = "Name 2",
    tradingName = None,
    yearOfMigration = None,
    accountingPeriods = Seq.empty,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = None,
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = "example",
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = "example"
  )

}
