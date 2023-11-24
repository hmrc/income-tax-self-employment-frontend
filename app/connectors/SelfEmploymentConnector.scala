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
import connectors.httpParser.SendExpensesAnswersHttpParser.{SendExpensesAnswersResponse, SendExpensesAnswersHttpReads}
import models.journeys.expenses.ExpensesData
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig) {

  def getBusinesses(nino: String, mtditid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/details/$nino/list")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getBusiness(nino: String, businessId: String, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/details/$nino/$businessId")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getJourneyState(businessId: String, journey: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[JourneyStateResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear")
    http.GET[JourneyStateResponse](url)(JourneyStateHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def saveJourneyState(businessId: String, journey: String, taxYear: Int, complete: Boolean, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[JourneyStateResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/completed-section/$businessId/$journey/$taxYear/$complete")

    http.PUT[String, JourneyStateResponse](url, "")(
      JourneyStateHttpWrites,
      JourneyStateHttpReads,
      hc.withExtraHeaders(headers = "mtditid" -> mtditid),
      ec)
  }

  def getCompletedTradesWithStatuses(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetTradesStatusResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/journey-states/$nino/$taxYear")
    http.GET[GetTradesStatusResponse](url)(GetTradesStatusHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def sendExpensesAnswers[T](data: ExpensesData, answers: T)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      writes: Writes[T]): Future[SendExpensesAnswersResponse] = {

    val url = buildUrl(
      s"/income-tax-self-employment/send-expenses-answers/${data.journey.toString}/${data.taxYear.value}/${data.businessId.value}/${data.nino.value}")

    http.POST[T, SendExpensesAnswersResponse](url, answers)(
      wts = writes,
      rds = SendExpensesAnswersHttpReads,
      hc = hc.withExtraHeaders(headers = "mtditid" -> data.mtditid),
      ec = ec)
  }

  private def buildUrl(url: String) = appConfig.selfEmploymentBEBaseUrl + url

}
