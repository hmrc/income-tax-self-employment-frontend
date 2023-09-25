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

package connectors

import config.FrontendAppConfig
import connectors.httpParser.GetBusinessesHttpParser.{GetBusinessesHttpReads, GetBusinessesResponse}
import models.errors.APIErrorBody.APIStatusError
import models.requests.{BusinessData, BusinessDataWithStatus}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject()(val http: HttpClient, val appConfig: FrontendAppConfig)
                                       (implicit ec: ExecutionContext) {

//  def getBusinesses(nino: String): Future[GetBusinessesResponse] = {
//
//
//    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/business/$nino"
//    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc, ec)
//  }
//
//  def getBusiness(nino: String, businessId: String): Future[GetBusinessesResponse] = {
//
//    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/business/$nino/$businessId"
//    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc, ec)
//  }

  def getTradesWithStatus(nino: String, taxYear: Int): Future[Either[APIStatusError, Seq[BusinessDataWithStatus]]] = {
    val bdws1 = BusinessDataWithStatus(BusinessData(
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
    ), true)
    val bdws2 = BusinessDataWithStatus(BusinessData(
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
    ), false)
    Future(Right(Seq(bdws1, bdws2)))
  }

//  def getJourneyState(nino: String, taxYear: Int, businessId: String, journey: String): Future[Either[APIStatusError, String]] = {
//
//    val url = appConfig.selfEmploymentBEBaseUrl + s"/completed-section/$businessId/$journey/$taxYear"
//    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc, ec)
//  }

  def getJourneyStateMock(taxYear: Int, businessId: String, journey: String): Future[Either[APIStatusError, String]] = {
    Future(Right(if (businessId.equals("id1")) "completed" else "notStarted"))
    //can return a NoContent if 'notStarted', Ok(Boolean) for 'completed' or 'inProgress'
  }

}
