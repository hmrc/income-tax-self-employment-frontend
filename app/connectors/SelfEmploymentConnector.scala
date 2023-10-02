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
import connectors.httpParser.GetTradesStatusHttpParser.{GetTradesStatusHttpReads, GetTradesStatusResponse}
import connectors.httpParser.JourneyStateParser.{JourneyStateHttpReads, JourneyStateHttpWrites, JourneyStateResponse}
import models._
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyStatus
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject()(http: HttpClient, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def getBusinesses(nino: String, mtditid: String)
                   (implicit hc: HeaderCarrier): Future[GetBusinessesResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/individuals/business/details/$nino"
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getBusiness(nino: String, businessId: String, mtditid: String)
                 (implicit hc: HeaderCarrier): Future[GetBusinessesResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/individuals/business/details/$nino/$businessId"
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getJourneyState(businessId: String, journey: String, taxYear: Int, mtditid: String)
                     (implicit hc: HeaderCarrier): Future[JourneyStateResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear/"
    http.GET[JourneyStateResponse](url)(JourneyStateHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def saveJourneyState(businessId: String, journey: String, taxYear: Int, complete: Boolean, mtditid: String)
                      (implicit hc: HeaderCarrier): Future[JourneyStateResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear/$complete"

    http.PUT[String, JourneyStateResponse](url, "")(
      JourneyStateHttpWrites, JourneyStateHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getCompletedTradesWithStatuses(nino: String, taxYear: Int, mtditid: String)
                                    (implicit hc: HeaderCarrier): Future[GetTradesStatusResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/trade-statuses/$nino/$taxYear"
    http.GET[GetTradesStatusResponse](url)(GetTradesStatusHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getCompletedTradesWithStatusMock(nino: String, taxYear: Int, mtditid: String): Future[GetTradesStatusResponse] = {
    Future(Right(Seq(
      TradesJourneyStatuses("BusinessId1", Some("TradingName1"), Seq(
        JourneyStatus(Abroad, Some(true)),
        JourneyStatus(Income, Some(false)),
        JourneyStatus(Expenses, None),
        JourneyStatus(NationalInsurance, None)
      )),
      TradesJourneyStatuses("BusinessId2", None, Seq.empty)
    )))
  }

}
