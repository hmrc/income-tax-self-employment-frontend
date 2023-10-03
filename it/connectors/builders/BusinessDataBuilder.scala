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

package connectors.builders

object BusinessDataBuilder {

  lazy val aGetBusinessDataRequestStr: String =
  """ |[
      |   {
      |      "businessId":"SJPR05893938418",
      |      "typeOfBusiness":"self-employment",
      |      "tradingName":"string",
      |      "yearOfMigration":"2022",
      |      "accountingPeriods":[
      |         {
      |            "start":"2023-02-29",
      |            "end":"2024-02-29"
      |         }
      |      ],
      |      "firstAccountingPeriodStartDate":"2019-09-30",
      |      "firstAccountingPeriodEndDate":"2020-02-29",
      |      "latencyDetails":{
      |         "latencyEndDate":"2020-02-27",
      |         "taxYear1":"2019",
      |         "latencyIndicator1":"A",
      |         "taxYear2":"2020",
      |         "latencyIndicator2":"A"
      |      },
      |      "accountingType":"ACCRUAL",
      |      "commencementDate":"2023-04-06",
      |      "cessationDate":"2024-04-05",
      |      "businessAddressLineOne":"string",
      |      "businessAddressLineTwo  ":"string",
      |      "businessAddressLineThree":"string",
      |      "businessAddressLineFour ":"string",
      |      "businessAddressPostcode ":"string",
      |      "businessAddressCountryCode":"GB"
      |   }
      |]
      |""".stripMargin


}
